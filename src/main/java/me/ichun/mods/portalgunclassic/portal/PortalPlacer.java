package me.ichun.mods.portalgunclassic.portal;

import me.ichun.mods.portalgunclassic.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Optional;

public final class PortalPlacer
{
    public record Spot(BlockPos lower, Direction facing) {}

    private PortalPlacer() {}

    public static Optional<Spot> find(Level level, BlockHitResult hit)
    {
        if (hit.getType() != HitResult.Type.BLOCK)
        {
            return Optional.empty();
        }
        Direction face = hit.getDirection();
        BlockPos front = hit.getBlockPos().relative(face);
        return candidates(hit, front, face).stream()
                .map(lower -> new Spot(lower, face))
                .filter(spot -> isValid(level, spot))
                .findFirst();
    }

    private static List<BlockPos> candidates(BlockHitResult hit, BlockPos front, Direction face)
    {
        if (!PortalStructure.isTall(face))
        {
            return List.of(front);
        }
        boolean upperHalf = hit.getLocation().y - front.getY() >= 0.5;
        return upperHalf ? List.of(front, front.below()) : List.of(front.below(), front);
    }

    private static boolean isValid(Level level, Spot spot)
    {
        return PortalStructure.cells(spot.lower(), spot.facing()).stream()
                .allMatch(cell -> isFree(level, cell) && PortalStructure.isSupported(level, cell, spot.facing()));
    }

    private static boolean isFree(Level level, BlockPos cell)
    {
        BlockState state = level.getBlockState(cell);
        return state.canBeReplaced() || state.is(ModBlocks.PORTAL);
    }
}
