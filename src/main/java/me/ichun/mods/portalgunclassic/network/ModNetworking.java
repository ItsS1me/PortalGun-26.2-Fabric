package me.ichun.mods.portalgunclassic.network;

import me.ichun.mods.portalgunclassic.item.PortalGunItem;
import me.ichun.mods.portalgunclassic.portal.PortalAction;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ModNetworking
{
    private ModNetworking() {}

    public static void init()
    {
        PayloadTypeRegistry.serverboundPlay().register(PortalGunActionPayload.TYPE, PortalGunActionPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PortalGunActionPayload.TYPE,
                (payload, context) -> handle(context.player(), payload));
    }

    private static void handle(ServerPlayer player, PortalGunActionPayload payload)
    {
        PortalAction action = PortalAction.byId(payload.action());
        if (action == null || !(player.level() instanceof ServerLevel level))
        {
            return;
        }
        ItemStack gun = findGun(player);
        if (gun != null)
        {
            PortalGunItem.perform(level, player, gun, action);
        }
    }

    private static ItemStack findGun(ServerPlayer player)
    {
        if (player.getMainHandItem().getItem() instanceof PortalGunItem)
        {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof PortalGunItem)
        {
            return player.getOffhandItem();
        }
        return null;
    }
}
