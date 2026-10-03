package me.ichun.mods.portalgunclassic.portal;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PortalColor implements StringRepresentable
{
    BLUE("blue"),
    ORANGE("orange");

    public static final Codec<PortalColor> CODEC = StringRepresentable.fromEnum(PortalColor::values);
    public static final StreamCodec<ByteBuf, PortalColor> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], PortalColor::ordinal);

    private final String name;

    PortalColor(String name)
    {
        this.name = name;
    }

    public PortalColor opposite()
    {
        return this == BLUE ? ORANGE : BLUE;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }
}
