package me.ichun.mods.portalgunclassic.portal;

import me.ichun.mods.portalgunclassic.block.PortalBlock;
import me.ichun.mods.portalgunclassic.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.List;

public final class PortalStructure
{
    private PortalStructure() {}

    public static boolean isTall(Direction facing)
    {
        return facing.getAxis().isHorizontal();
    }

    public static BlockPos lowerOf(BlockPos pos, BlockState state)
    {
        return state.getValue(PortalBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    public static List<BlockPos> cells(BlockPos lower, Direction facing)
    {
        return isTall(facing) ? List.of(lower, lower.above()) : List.of(lower);
    }

    public static boolean isIntact(Level level, BlockPos pos, BlockState state)
    {
        Direction facing = state.getValue(PortalBlock.FACING);
        return cells(lowerOf(pos, state), facing).stream()
                .allMatch(cell -> matches(level, cell, state) && isSupported(level, cell, facing));
    }

    public static void remove(Level level, BlockPos pos, BlockState state)
    {
        Direction facing = state.getValue(PortalBlock.FACING);
        for (BlockPos cell : cells(lowerOf(pos, state), facing))
        {
            if (matches(level, cell, state))
            {
                level.removeBlock(cell, false);
            }
        }
    }

    public static boolean isSupported(Level level, BlockPos cell, Direction facing)
    {
        BlockPos wall = cell.relative(facing.getOpposite());
        return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
    }

    private static boolean matches(Level level, BlockPos cell, BlockState reference)
    {
        BlockState state = level.getBlockState(cell);
        return state.is(ModBlocks.PORTAL)
                && state.getValue(PortalBlock.FACING) == reference.getValue(PortalBlock.FACING)
                && state.getValue(PortalBlock.COLOR) == reference.getValue(PortalBlock.COLOR);
    }
}
