package me.ichun.mods.portalgunclassic.block;

import me.ichun.mods.portalgunclassic.portal.PortalConfig;
import me.ichun.mods.portalgunclassic.portal.PortalStructure;
import me.ichun.mods.portalgunclassic.portal.PortalTarget;
import me.ichun.mods.portalgunclassic.portal.PortalTeleporter;
import me.ichun.mods.portalgunclassic.registry.ModBlockEntities;
import me.ichun.mods.portalgunclassic.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class PortalBlockEntity extends BlockEntity
{
    private static final String PARTNER_KEY = "partner";

    private GlobalPos partner;

    public PortalBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.PORTAL, pos, state);
    }

    public GlobalPos getPartner()
    {
        return partner;
    }

    public void setPartner(GlobalPos partner)
    {
        this.partner = partner;
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);
        if (partner != null)
        {
            output.store(PARTNER_KEY, GlobalPos.CODEC, partner);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);
        partner = input.read(PARTNER_KEY, GlobalPos.CODEC).orElse(null);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PortalBlockEntity portal)
    {
        if (!(level instanceof ServerLevel server))
        {
            return;
        }
        if (!PortalStructure.isIntact(server, pos, state))
        {
            PortalStructure.remove(server, pos, state);
            return;
        }
        if (state.getValue(PortalBlock.HALF) == DoubleBlockHalf.LOWER)
        {
            portal.tickLower(server, state);
        }
    }

    private void tickLower(ServerLevel level, BlockState state)
    {
        List<Entity> touching = PortalTeleporter.touching(level, getBlockPos(), state);
        if (touching.isEmpty())
        {
            return;
        }
        PortalTarget target = resolveTarget(level, state);
        if (target != null)
        {
            touching.forEach(entity -> PortalTeleporter.teleport(level, entity, getBlockPos(), state, target));
        }
    }

    public PortalTarget resolveTarget(ServerLevel level, BlockState state)
    {
        ServerLevel targetLevel = targetLevel(level);
        if (targetLevel == null)
        {
            return null;
        }
        if (!(targetLevel.getBlockEntity(partner.pos()) instanceof PortalBlockEntity other) || !isOppositeLower(state, other))
        {
            setPartner(null);
            return null;
        }
        GlobalPos self = GlobalPos.of(level.dimension(), getBlockPos());
        if (other.partner == null)
        {
            other.setPartner(self);
        }
        return self.equals(other.partner) ? new PortalTarget(targetLevel, partner.pos(), other.getBlockState()) : null;
    }

    private ServerLevel targetLevel(ServerLevel level)
    {
        if (partner == null)
        {
            return null;
        }
        boolean sameDimension = partner.dimension().equals(level.dimension());
        if (!sameDimension && !PortalConfig.get().crossDimension)
        {
            return null;
        }
        return level.getServer().getLevel(partner.dimension());
    }

    private static boolean isOppositeLower(BlockState state, PortalBlockEntity other)
    {
        BlockState otherState = other.getBlockState();
        return otherState.is(ModBlocks.PORTAL)
                && otherState.getValue(PortalBlock.HALF) == DoubleBlockHalf.LOWER
                && otherState.getValue(PortalBlock.COLOR) == state.getValue(PortalBlock.COLOR).opposite();
    }
}
