package me.ichun.mods.portalgunclassic;

import me.ichun.mods.portalgunclassic.network.ModNetworking;
import me.ichun.mods.portalgunclassic.portal.PortalWatcher;
import me.ichun.mods.portalgunclassic.registry.ModBlockEntities;
import me.ichun.mods.portalgunclassic.registry.ModBlocks;
import me.ichun.mods.portalgunclassic.registry.ModComponents;
import me.ichun.mods.portalgunclassic.registry.ModItems;
import me.ichun.mods.portalgunclassic.registry.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public class PortalGunMod implements ModInitializer
{
    public static final String MOD_ID = "portalgunclassic";

    public static Identifier id(String path)
    {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize()
    {
        ModComponents.init();
        ModSounds.init();
        ModBlocks.init();
        ModBlockEntities.init();
        ModItems.init();
        ModNetworking.init();
        PortalWatcher.init();
    }
}
