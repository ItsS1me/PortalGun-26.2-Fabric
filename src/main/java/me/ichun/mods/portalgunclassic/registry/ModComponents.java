package me.ichun.mods.portalgunclassic.registry;

import me.ichun.mods.portalgunclassic.PortalGunMod;
import me.ichun.mods.portalgunclassic.portal.PortalColor;
import me.ichun.mods.portalgunclassic.portal.PortalLinks;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.UnaryOperator;

public final class ModComponents
{
    public static final DataComponentType<PortalColor> PORTAL_COLOR = register("portal_color",
            builder -> builder.persistent(PortalColor.CODEC).networkSynchronized(PortalColor.STREAM_CODEC));

    public static final DataComponentType<PortalLinks> PORTAL_LINKS = register("portal_links",
            builder -> builder.persistent(PortalLinks.CODEC).networkSynchronized(PortalLinks.STREAM_CODEC));

    private ModComponents() {}

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> configure)
    {
        DataComponentType<T> type = configure.apply(DataComponentType.builder()).build();
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, PortalGunMod.id(name), type);
    }

    public static void init() {}
}
