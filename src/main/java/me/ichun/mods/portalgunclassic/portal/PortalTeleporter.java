package me.ichun.mods.portalgunclassic.portal;

import me.ichun.mods.portalgunclassic.block.PortalBlock;
import me.ichun.mods.portalgunclassic.registry.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

public final class PortalTeleporter
{
    private static final double TRIGGER_DEPTH = 0.5;
    private static final double EXIT_OFFSET = 0.3;
    private static final double MIN_EXIT_SPEED = 0.25;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG = Boolean.getBoolean("portalgun.debug");

    private static final Map<UUID, Long> LAST_TELEPORT = new HashMap<>();

    private PortalTeleporter() {}

    public static void reset()
    {
        LAST_TELEPORT.clear();
    }

    public static List<Entity> touching(ServerLevel level, BlockPos lower, BlockState state)
    {
        long now = level.getGameTime();
        AABB zone = triggerZone(lower, state.getValue(PortalBlock.FACING));
        return level.getEntitiesOfClass(Entity.class, zone, entity -> canTeleport(entity, now));
    }

    public static boolean canEnter(ServerLevel level, Entity entity, BlockPos lower, BlockState state)
    {
        AABB zone = triggerZone(lower, state.getValue(PortalBlock.FACING));
        return canTeleport(entity, level.getGameTime()) && entity.getBoundingBox().intersects(zone);
    }

    public static void teleport(ServerLevel level, Entity entity, BlockPos fromLower, BlockState fromState, PortalTarget target)
    {
        Direction entry = fromState.getValue(PortalBlock.FACING);
        Direction exit = target.state().getValue(PortalBlock.FACING);
        Vec3 velocity = exitVelocity(entity.getDeltaMovement(), entry, exit);
        float yaw = exitYaw(entity.getYRot(), entry, exit);
        Vec3 position = exitPosition(target.pos(), exit, entity);

        recordTeleport(entity, level.getGameTime(), fromLower, target);
        playSound(level, fromLower, ModSounds.ENTER);
        entity.teleport(new TeleportTransition(target.level(), position, velocity, yaw, entity.getXRot(), moved -> moved.resetFallDistance()));
        playSound(target.level(), target.pos(), ModSounds.EXIT);
    }

    private static boolean canTeleport(Entity entity, long now)
    {
        if (!entity.isAlive() || entity.isPassenger())
        {
            return false;
        }
        if (!(entity instanceof Player) && !PortalConfig.get().teleportOtherEntities)
        {
            return false;
        }
        Long last = LAST_TELEPORT.get(entity.getUUID());
        return last == null || now - last >= PortalConfig.get().cooldownTicks;
    }

    private static void recordTeleport(Entity entity, long now, BlockPos from, PortalTarget target)
    {
        if (DEBUG)
        {
            LOGGER.info("Teleporting {} from {} to {} in {}", entity.getName().getString(), from, target.pos(), target.level().dimension());
        }
        LAST_TELEPORT.put(entity.getUUID(), now);
        if (LAST_TELEPORT.size() > 256)
        {
            LAST_TELEPORT.values().removeIf(time -> now - time >= PortalConfig.get().cooldownTicks);
        }
    }

    private static void playSound(ServerLevel level, BlockPos pos, SoundEvent sound)
    {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, PortalConfig.get().soundVolume, 1.0F);
    }

    private static AABB triggerZone(BlockPos lower, Direction facing)
    {
        double x0 = lower.getX();
        double y0 = lower.getY();
        double z0 = lower.getZ();
        double x1 = x0 + 1;
        double y1 = y0 + (PortalStructure.isTall(facing) ? 2 : 1);
        double z1 = z0 + 1;
        switch (facing)
        {
            case NORTH -> z0 = z1 - TRIGGER_DEPTH;
            case SOUTH -> z1 = z0 + TRIGGER_DEPTH;
            case WEST -> x0 = x1 - TRIGGER_DEPTH;
            case EAST -> x1 = x0 + TRIGGER_DEPTH;
            case UP -> y1 = y0 + TRIGGER_DEPTH;
            case DOWN -> y0 = y1 - TRIGGER_DEPTH;
        }
        return new AABB(x0, y0, z0, x1, y1, z1);
    }

    private static float turnDegrees(Direction entry, Direction exit)
    {
        return Mth.wrapDegrees(exit.toYRot() - (entry.toYRot() + 180.0F));
    }

    private static float exitYaw(float yaw, Direction entry, Direction exit)
    {
        if (PortalStructure.isTall(entry) && PortalStructure.isTall(exit))
        {
            return yaw + turnDegrees(entry, exit);
        }
        return PortalStructure.isTall(exit) ? exit.toYRot() : yaw;
    }

    private static Vec3 exitVelocity(Vec3 velocity, Direction entry, Direction exit)
    {
        if (PortalStructure.isTall(entry) && PortalStructure.isTall(exit))
        {
            return pushOut(rotateYaw(velocity, turnDegrees(entry, exit)), exit);
        }
        double speed = Math.max(velocity.length(), MIN_EXIT_SPEED);
        return new Vec3(exit.getStepX(), exit.getStepY(), exit.getStepZ()).scale(speed);
    }

    private static Vec3 rotateYaw(Vec3 velocity, float turnDegrees)
    {
        // Vec3.yRot(a) lowers the yaw of a direction by a, so adding turnDegrees to the yaw means rotating by -turnDegrees
        return velocity.yRot(-turnDegrees * Mth.DEG_TO_RAD);
    }

    private static Vec3 pushOut(Vec3 velocity, Direction exit)
    {
        double along = velocity.x * exit.getStepX() + velocity.z * exit.getStepZ();
        double missing = Math.max(0.0, MIN_EXIT_SPEED - along);
        return velocity.add(exit.getStepX() * missing, 0.0, exit.getStepZ() * missing);
    }

    private static Vec3 exitPosition(BlockPos lower, Direction exit, Entity entity)
    {
        Vec3 base = Vec3.atBottomCenterOf(lower);
        return switch (exit)
        {
            case UP -> base.add(0.0, 0.05, 0.0);
            case DOWN -> base.add(0.0, 0.95 - entity.getBbHeight(), 0.0);
            default -> base.add(exit.getStepX() * EXIT_OFFSET, 0.0, exit.getStepZ() * EXIT_OFFSET);
        };
    }
}
