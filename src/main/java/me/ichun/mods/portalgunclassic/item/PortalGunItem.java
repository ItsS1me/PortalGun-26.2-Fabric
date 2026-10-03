package me.ichun.mods.portalgunclassic.item;

import me.ichun.mods.portalgunclassic.portal.PortalAction;
import me.ichun.mods.portalgunclassic.portal.PortalColor;
import me.ichun.mods.portalgunclassic.portal.PortalFiring;
import me.ichun.mods.portalgunclassic.registry.ModComponents;
import me.ichun.mods.portalgunclassic.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PortalGunItem extends Item
{
    public PortalGunItem(Properties properties)
    {
        super(properties);
    }

    public static PortalColor colorOf(ItemStack stack)
    {
        return stack.getOrDefault(ModComponents.PORTAL_COLOR, PortalColor.BLUE);
    }

    public static void toggleColor(ItemStack stack)
    {
        stack.set(ModComponents.PORTAL_COLOR, colorOf(stack).opposite());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        if (level instanceof ServerLevel server)
        {
            handle(server, player, player.getItemInHand(hand));
        }
        return InteractionResult.SUCCESS;
    }

    private static void handle(ServerLevel level, Player player, ItemStack gun)
    {
        if (player.isShiftKeyDown())
        {
            switchColor(level, player, gun);
            return;
        }
        PortalFiring.fire(level, player, gun);
    }

    public static void perform(ServerLevel level, Player player, ItemStack gun, PortalAction action)
    {
        switch (action)
        {
            case SWITCH_COLOR -> switchColor(level, player, gun);
            case FIRE_BLUE ->
            {
                gun.set(ModComponents.PORTAL_COLOR, PortalColor.BLUE);
                PortalFiring.fire(level, player, gun);
            }
            case FIRE_ORANGE ->
            {
                gun.set(ModComponents.PORTAL_COLOR, PortalColor.ORANGE);
                PortalFiring.fire(level, player, gun);
            }
            case CLEAR_PORTALS -> PortalFiring.clear(level, player, gun);
        }
    }

    private static void switchColor(ServerLevel level, Player player, ItemStack gun)
    {
        toggleColor(gun);
        level.playSound(null, player.blockPosition(), ModSounds.ACTIVE, SoundSource.PLAYERS, 0.4F, 1.0F);
    }
}
