package me.ichun.mods.portalgunclassic.registry;

import me.ichun.mods.portalgunclassic.PortalGunMod;
import me.ichun.mods.portalgunclassic.block.PortalBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class ModBlocks
{
    public static final Block PORTAL = register("portal", PortalBlock::new, portalProperties());

    private ModBlocks() {}

    private static BlockBehaviour.Properties portalProperties()
    {
        return BlockBehaviour.Properties.of()
                .noCollision()
                .noOcclusion()
                .instabreak()
                .noLootTable()
                .lightLevel(state -> 8);
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties)
    {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, PortalGunMod.id(name));
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
    }

    public static void init() {}
}
