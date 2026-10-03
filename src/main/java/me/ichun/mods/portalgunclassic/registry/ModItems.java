package me.ichun.mods.portalgunclassic.registry;

import me.ichun.mods.portalgunclassic.PortalGunMod;
import me.ichun.mods.portalgunclassic.item.PortalGunItem;
import me.ichun.mods.portalgunclassic.portal.PortalColor;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public final class ModItems
{
    public static final Item PORTAL_CORE = register("portal_core", Item::new, new Item.Properties());
    public static final Item PORTAL_GUN = register("portal_gun", PortalGunItem::new,
            new Item.Properties().stacksTo(1).component(ModComponents.PORTAL_COLOR, PortalColor.BLUE));

    private ModItems() {}

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties)
    {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, PortalGunMod.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
    }

    private static void addToCreativeTabs()
    {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> output.accept(PORTAL_GUN));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> output.accept(PORTAL_CORE));
    }

    public static void init()
    {
        addToCreativeTabs();
    }
}
