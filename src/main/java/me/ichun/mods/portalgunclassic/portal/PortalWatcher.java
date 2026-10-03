package me.ichun.mods.portalgunclassic.portal;

import me.ichun.mods.portalgunclassic.block.PortalBlockEntity;
import me.ichun.mods.portalgunclassic.registry.ModBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class PortalWatcher
{
    private PortalWatcher() {}

    public static void init()
    {
        ServerTickEvents.END_LEVEL_TICK.register(PortalWatcher::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> PortalTeleporter.reset());
    }

    private static void tick(ServerLevel level)
    {
        for (Player player : level.players())
        {
            if (player.isAlive() && !player.isSpectator())
            {
                check(level, player);
            }
        }
    }

    private static void check(ServerLevel level, Player player)
    {
        AABB box = player.getBoundingBox().inflate(0.1);
        for (BlockPos pos : BlockPos.betweenClosed(
                Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ)))
        {
            BlockState state = level.getBlockState(pos);
            if (!state.is(ModBlocks.PORTAL))
            {
                continue;
            }
            BlockPos lower = PortalStructure.lowerOf(pos, state).immutable();
            BlockState lowerState = level.getBlockState(lower);
            if (lowerState.is(ModBlocks.PORTAL)
                    && PortalTeleporter.canEnter(level, player, lower, lowerState)
                    && level.getBlockEntity(lower) instanceof PortalBlockEntity portal)
            {
                PortalTarget target = portal.resolveTarget(level, lowerState);
                if (target != null)
                {
                    PortalTeleporter.teleport(level, player, lower, lowerState, target);
                    return;
                }
            }
        }
    }
}
