package me.ichun.mods.portalgunclassic.registry;

import me.ichun.mods.portalgunclassic.PortalGunMod;
import me.ichun.mods.portalgunclassic.block.PortalBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities
{
    public static final BlockEntityType<PortalBlockEntity> PORTAL = register("portal", PortalBlockEntity::new, ModBlocks.PORTAL);

    private ModBlockEntities() {}

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> factory,
            Block... blocks)
    {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, PortalGunMod.id(name),
                FabricBlockEntityTypeBuilder.<T>create(factory, blocks).build());
    }

    public static void init() {}
}
