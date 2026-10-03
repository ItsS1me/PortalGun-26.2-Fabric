package me.ichun.mods.portalgunclassic;

import com.mojang.blaze3d.platform.InputConstants;
import me.ichun.mods.portalgunclassic.item.PortalGunItem;
import me.ichun.mods.portalgunclassic.network.PortalGunActionPayload;
import me.ichun.mods.portalgunclassic.portal.PortalAction;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class PortalGunClient implements ClientModInitializer
{
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(PortalGunMod.id("portal_gun"));

    private static KeyMapping switchColorKey;
    private static KeyMapping fireBlueKey;
    private static KeyMapping fireOrangeKey;
    private static KeyMapping clearKey;

    @Override
    public void onInitializeClient()
    {
        switchColorKey = register("switch_color", InputConstants.KEY_R);
        fireBlueKey = register("fire_blue", -1);
        fireOrangeKey = register("fire_orange", -1);
        clearKey = register("clear_portals", -1);

        ClientTickEvents.END_CLIENT_TICK.register(PortalGunClient::onTick);
    }

    private static KeyMapping register(String name, int defaultKey)
    {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key." + PortalGunMod.MOD_ID + "." + name,
                InputConstants.Type.KEYSYM,
                defaultKey,
                CATEGORY));
    }

    private static void onTick(Minecraft client)
    {
        if (client.player == null)
        {
            return;
        }
        boolean holdingGun = client.player.getMainHandItem().getItem() instanceof PortalGunItem
                || client.player.getOffhandItem().getItem() instanceof PortalGunItem;

        poll(switchColorKey, PortalAction.SWITCH_COLOR, holdingGun);
        poll(fireBlueKey, PortalAction.FIRE_BLUE, holdingGun);
        poll(fireOrangeKey, PortalAction.FIRE_ORANGE, holdingGun);
        poll(clearKey, PortalAction.CLEAR_PORTALS, holdingGun);
    }

    private static void poll(KeyMapping key, PortalAction action, boolean holdingGun)
    {
        while (key.consumeClick())
        {
            if (holdingGun)
            {
                ClientPlayNetworking.send(PortalGunActionPayload.of(action));
            }
        }
    }
}
