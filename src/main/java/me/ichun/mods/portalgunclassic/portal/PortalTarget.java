package me.ichun.mods.portalgunclassic.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public record PortalTarget(ServerLevel level, BlockPos pos, BlockState state) {}
