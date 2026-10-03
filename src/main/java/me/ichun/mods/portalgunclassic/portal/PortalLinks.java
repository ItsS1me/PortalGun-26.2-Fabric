package me.ichun.mods.portalgunclassic.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record PortalLinks(Optional<GlobalPos> blue, Optional<GlobalPos> orange)
{
    public static final PortalLinks EMPTY = new PortalLinks(Optional.empty(), Optional.empty());

    public static final Codec<PortalLinks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.optionalFieldOf("blue").forGetter(PortalLinks::blue),
            GlobalPos.CODEC.optionalFieldOf("orange").forGetter(PortalLinks::orange)
    ).apply(instance, PortalLinks::new));

    public static final StreamCodec<ByteBuf, PortalLinks> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(GlobalPos.STREAM_CODEC), PortalLinks::blue,
            ByteBufCodecs.optional(GlobalPos.STREAM_CODEC), PortalLinks::orange,
            PortalLinks::new
    );

    public Optional<GlobalPos> get(PortalColor color)
    {
        return color == PortalColor.BLUE ? blue : orange;
    }

    public PortalLinks with(PortalColor color, GlobalPos pos)
    {
        Optional<GlobalPos> value = Optional.of(pos);
        return color == PortalColor.BLUE ? new PortalLinks(value, orange) : new PortalLinks(blue, value);
    }
}
