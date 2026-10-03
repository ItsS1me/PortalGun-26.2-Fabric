package me.ichun.mods.portalgunclassic.portal;

public enum PortalAction
{
    SWITCH_COLOR,
    FIRE_BLUE,
    FIRE_ORANGE,
    CLEAR_PORTALS;

    public static PortalAction byId(int id)
    {
        PortalAction[] all = values();
        return id >= 0 && id < all.length ? all[id] : null;
    }
}
