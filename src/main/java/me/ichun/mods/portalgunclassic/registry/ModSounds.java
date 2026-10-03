package me.ichun.mods.portalgunclassic.registry;

import me.ichun.mods.portalgunclassic.PortalGunMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds
{
    public static final SoundEvent FIRE_BLUE = register("fire_blue");
    public static final SoundEvent FIRE_ORANGE = register("fire_orange");
    public static final SoundEvent OPEN_BLUE = register("open_blue");
    public static final SoundEvent OPEN_ORANGE = register("open_orange");
    public static final SoundEvent FIZZLE = register("fizzle");
    public static final SoundEvent INVALID = register("invalid");
    public static final SoundEvent ENTER = register("enter");
    public static final SoundEvent EXIT = register("exit");
    public static final SoundEvent ACTIVE = register("active");

    private ModSounds() {}

    private static SoundEvent register(String name)
    {
        Identifier id = PortalGunMod.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {}
}
