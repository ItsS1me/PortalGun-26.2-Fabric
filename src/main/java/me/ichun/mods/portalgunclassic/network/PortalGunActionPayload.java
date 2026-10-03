package me.ichun.mods.portalgunclassic.network;

import me.ichun.mods.portalgunclassic.PortalGunMod;
import me.ichun.mods.portalgunclassic.portal.PortalAction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PortalGunActionPayload(int action) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<PortalGunActionPayload> TYPE =
            new CustomPacketPayload.Type<>(PortalGunMod.id("gun_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PortalGunActionPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, PortalGunActionPayload::action, PortalGunActionPayload::new);

    public static PortalGunActionPayload of(PortalAction action)
    {
        return new PortalGunActionPayload(action.ordinal());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
