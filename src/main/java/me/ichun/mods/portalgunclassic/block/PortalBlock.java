package me.ichun.mods.portalgunclassic.block;

import com.mojang.serialization.MapCodec;
import me.ichun.mods.portalgunclassic.portal.PortalColor;
import me.ichun.mods.portalgunclassic.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class PortalBlock extends Block implements EntityBlock
{
    public static final MapCodec<PortalBlock> CODEC = simpleCodec(PortalBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final EnumProperty<PortalColor> COLOR = EnumProperty.create("color", PortalColor.class);

    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, Block.box(0, 0, 15, 16, 16, 16),
            Direction.SOUTH, Block.box(0, 0, 0, 16, 16, 1),
            Direction.EAST, Block.box(0, 0, 0, 1, 16, 16),
            Direction.WEST, Block.box(15, 0, 0, 16, 16, 16),
            Direction.UP, Block.box(0, 0, 0, 16, 1, 16),
            Direction.DOWN, Block.box(0, 15, 0, 16, 16, 16)
    );

    public PortalBlock(Properties properties)
    {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(COLOR, PortalColor.BLUE));
    }

    public static BlockState stateFor(BlockState base, Direction facing, DoubleBlockHalf half, PortalColor color)
    {
        return base.setValue(FACING, facing).setValue(HALF, half).setValue(COLOR, color);
    }

    @Override
    protected MapCodec<? extends Block> codec()
    {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, HALF, COLOR);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new PortalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide() || type != ModBlockEntities.PORTAL)
        {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> PortalBlockEntity.tick(tickLevel, pos, tickState, (PortalBlockEntity) entity);
    }
}
