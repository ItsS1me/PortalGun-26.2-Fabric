package me.ichun.mods.portalgunclassic.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

final class ConfigSlider extends AbstractSliderButton
{
    private final double min;
    private final double max;
    private final DoubleConsumer setter;
    private final DoubleFunction<Component> label;

    ConfigSlider(int x, int y, int width, int height, double min, double max, double current,
                 DoubleConsumer setter, DoubleFunction<Component> label)
    {
        super(x, y, width, height, Component.empty(), (current - min) / (max - min));
        this.min = min;
        this.max = max;
        this.setter = setter;
        this.label = label;
        updateMessage();
    }

    private double actual()
    {
        return min + (max - min) * value;
    }

    @Override
    protected void updateMessage()
    {
        if (label != null)
        {
            setMessage(label.apply(actual()));
        }
    }

    @Override
    protected void applyValue()
    {
        setter.accept(actual());
    }
}
