package me.ichun.mods.portalgunclassic.client;

import me.ichun.mods.portalgunclassic.portal.PortalConfig;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class PortalConfigScreen extends Screen
{
    private static final int WIDTH = 220;
    private static final int HEIGHT = 20;
    private static final int ROW = 24;

    private final Screen parent;
    private final PortalConfig config = PortalConfig.get();

    public PortalConfigScreen(Screen parent)
    {
        super(Component.translatable("portalgunclassic.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        int x = (width - WIDTH) / 2;
        int y = height / 6;

        addRenderableWidget(new StringWidget(x, y - 18, WIDTH, 9, title, font));
        addRenderableWidget(rangeSlider(x, y));
        addRenderableWidget(cooldownSlider(x, y + ROW));
        addRenderableWidget(volumeSlider(x, y + ROW * 2));
        addRenderableWidget(toggle(x, y + ROW * 3, "crossDimension", () -> config.crossDimension, v -> config.crossDimension = v));
        addRenderableWidget(toggle(x, y + ROW * 4, "teleportOtherEntities", () -> config.teleportOtherEntities, v -> config.teleportOtherEntities = v));
        addRenderableWidget(doneButton(x, height - 28));
    }

    @Override
    public void onClose()
    {
        config.save();
        minecraft.gui.setScreen(parent);
    }

    private AbstractWidget rangeSlider(int x, int y)
    {
        return new ConfigSlider(x, y, WIDTH, HEIGHT, 8, 256, config.range,
                v -> config.range = Mth.floor(v),
                v -> Component.translatable("portalgunclassic.config.range", Mth.floor(v)));
    }

    private AbstractWidget cooldownSlider(int x, int y)
    {
        return new ConfigSlider(x, y, WIDTH, HEIGHT, 1, 100, config.cooldownTicks,
                v -> config.cooldownTicks = Mth.floor(v),
                v -> Component.translatable("portalgunclassic.config.cooldown", Mth.floor(v)));
    }

    private AbstractWidget volumeSlider(int x, int y)
    {
        return new ConfigSlider(x, y, WIDTH, HEIGHT, 0, 1, config.soundVolume,
                v -> config.soundVolume = (float) v,
                v -> Component.translatable("portalgunclassic.config.volume", Mth.floor(v * 100)));
    }

    private AbstractWidget toggle(int x, int y, String key, BooleanSupplier getter, Consumer<Boolean> setter)
    {
        Component caption = Component.translatable("portalgunclassic.config." + key);
        return Button.builder(CommonComponents.optionStatus(caption, getter.getAsBoolean()), button ->
        {
            setter.accept(!getter.getAsBoolean());
            button.setMessage(CommonComponents.optionStatus(caption, getter.getAsBoolean()));
        }).bounds(x, y, WIDTH, HEIGHT).build();
    }

    private AbstractWidget doneButton(int x, int y)
    {
        return Button.builder(CommonComponents.GUI_DONE, button -> onClose()).bounds(x, y, WIDTH, HEIGHT).build();
    }
}
