package me.ichun.mods.portalgunclassic.portal;

import me.ichun.mods.portalgunclassic.block.PortalBlock;
import me.ichun.mods.portalgunclassic.block.PortalBlockEntity;
import me.ichun.mods.portalgunclassic.item.PortalGunItem;
import me.ichun.mods.portalgunclassic.registry.ModBlocks;
import me.ichun.mods.portalgunclassic.registry.ModComponents;
import me.ichun.mods.portalgunclassic.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class PortalFiring
{
    private PortalFiring() {}

    public static void fire(ServerLevel level, Player player, ItemStack gun)
    {
        PortalColor color = PortalGunItem.colorOf(gun);
        play(level, player.blockPosition(), color == PortalColor.BLUE ? ModSounds.FIRE_BLUE : ModSounds.FIRE_ORANGE, SoundSource.PLAYERS);

        BlockHitResult hit = trace(level, player);
        Optional<PortalPlacer.Spot> spot = PortalPlacer.find(level, hit);
        if (spot.isEmpty())
        {
            play(level, BlockPos.containing(hit.getLocation()), ModSounds.INVALID, SoundSource.PLAYERS);
            return;
        }
        place(level, gun, color, spot.get());
    }

    public static void clear(ServerLevel level, Player player, ItemStack gun)
    {
        PortalLinks links = gun.getOrDefault(ModComponents.PORTAL_LINKS, PortalLinks.EMPTY);
        for (PortalColor color : PortalColor.values())
        {
            removePrevious(level, links, color);
        }
        gun.set(ModComponents.PORTAL_LINKS, PortalLinks.EMPTY);
        play(level, player.blockPosition(), ModSounds.FIZZLE, SoundSource.PLAYERS);
    }

    private static BlockHitResult trace(ServerLevel level, Player player)
    {
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.getLookAngle().scale(PortalConfig.get().range));
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
    }

    private static void place(ServerLevel level, ItemStack gun, PortalColor color, PortalPlacer.Spot spot)
    {
        PortalLinks links = gun.getOrDefault(ModComponents.PORTAL_LINKS, PortalLinks.EMPTY);
        removePrevious(level, links, color);
        setBlocks(level, spot, color);
        link(level, spot.lower(), links, color);
        gun.set(ModComponents.PORTAL_LINKS, links.with(color, GlobalPos.of(level.dimension(), spot.lower())));
        play(level, spot.lower(), color == PortalColor.BLUE ? ModSounds.OPEN_BLUE : ModSounds.OPEN_ORANGE, SoundSource.BLOCKS);
    }

    private static void removePrevious(ServerLevel level, PortalLinks links, PortalColor color)
    {
        livePortal(level, links.get(color), color)
                .ifPresent(target -> PortalStructure.remove(target.level(), target.pos(), target.state()));
    }

    private static void setBlocks(ServerLevel level, PortalPlacer.Spot spot, PortalColor color)
    {
        BlockState base = ModBlocks.PORTAL.defaultBlockState();
        level.setBlock(spot.lower(), PortalBlock.stateFor(base, spot.facing(), DoubleBlockHalf.LOWER, color), Block.UPDATE_ALL);
        if (PortalStructure.isTall(spot.facing()))
        {
            level.setBlock(spot.lower().above(), PortalBlock.stateFor(base, spot.facing(), DoubleBlockHalf.UPPER, color), Block.UPDATE_ALL);
        }
    }

    private static void link(ServerLevel level, BlockPos lower, PortalLinks links, PortalColor color)
    {
        Optional<PortalTarget> other = livePortal(level, links.get(color.opposite()), color.opposite())
                .filter(target -> canLink(level, target));
        GlobalPos self = GlobalPos.of(level.dimension(), lower);
        blockEntityAt(level, lower).ifPresent(mine -> mine.setPartner(other.map(PortalFiring::globalOf).orElse(null)));
        other.flatMap(target -> blockEntityAt(target.level(), target.pos())).ifPresent(theirs -> theirs.setPartner(self));
    }

    private static boolean canLink(ServerLevel level, PortalTarget target)
    {
        return PortalConfig.get().crossDimension || target.level().dimension().equals(level.dimension());
    }

    private static GlobalPos globalOf(PortalTarget target)
    {
        return GlobalPos.of(target.level().dimension(), target.pos());
    }

    private static Optional<PortalTarget> livePortal(ServerLevel from, Optional<GlobalPos> stored, PortalColor color)
    {
        return stored
                .map(global -> targetAt(from, global))
                .filter(target -> isPortalLower(target.state(), color));
    }

    private static PortalTarget targetAt(ServerLevel from, GlobalPos global)
    {
        ServerLevel owner = from.getServer().getLevel(global.dimension());
        return owner == null ? null : new PortalTarget(owner, global.pos(), owner.getBlockState(global.pos()));
    }

    private static boolean isPortalLower(BlockState state, PortalColor color)
    {
        return state.is(ModBlocks.PORTAL)
                && state.getValue(PortalBlock.COLOR) == color
                && state.getValue(PortalBlock.HALF) == DoubleBlockHalf.LOWER;
    }

    private static Optional<PortalBlockEntity> blockEntityAt(ServerLevel level, BlockPos pos)
    {
        return level.getBlockEntity(pos) instanceof PortalBlockEntity portal ? Optional.of(portal) : Optional.empty();
    }

    private static void play(ServerLevel level, BlockPos pos, SoundEvent sound, SoundSource source)
    {
        level.playSound(null, pos, sound, source, PortalConfig.get().soundVolume, 1.0F);
    }
}
