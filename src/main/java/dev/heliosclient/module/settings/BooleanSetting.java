package dev.heliosclient.module.settings;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.animation.Animation;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.misc.MapReader;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.function.BooleanSupplier;

public class BooleanSetting extends Setting<Boolean> {
    public boolean value;
    String description;
    Animation checkBoxAnimation = new Animation(EasingType.QUADRATIC_IN);

    public BooleanSetting(String name, String description, ISettingChange iSettingChange, boolean value, BooleanSupplier shouldRender, boolean defaultValue) {
        super(shouldRender, defaultValue);
        this.iSettingChange = iSettingChange;
        this.name = name;
        this.description = description;
        this.heightCompact = 14;
        this.value = value;
        checkBoxAnimation.startFading(true);
    }

    public BooleanSetting(String name, String description, ISettingChange iSettingChange, boolean defaultValue) {
        this(name, description, iSettingChange, defaultValue, () -> true, defaultValue);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        checkBoxAnimation.setFadeSpeed(HeliosClient.MC.getRenderTickCounter().getLastFrameDuration()/10f);


        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor());

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x + 175, y + 7, 10, 10, 2, 0.7f, 0xFFFFFFFF);
        AnimationUtils.drawFadingAndPoppingBox(drawContext,checkBoxAnimation, x + 176.7f, y + 8.7f, 6.3f, 6.3f, value ? HeliosClient.CLICKGUI.getAccentColor() : 0xFF222222, true, 2);

        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 50) {
            Tooltip.tooltip.changeText(description);
        }
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);

        String trimmedName = FontRenderers.Small_fxfontRenderer.trimToWidth(name, getWidthCompact());

        FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), trimmedName, x + 3, y + 4, ColorManager.INSTANCE.defaultTextColor());

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x + getWidthCompact() - 12, y + 4, 7, 7, 2, 0.6f, 0xFFFFFFFF);

        AnimationUtils.drawFadingAndPoppingBox(drawContext,checkBoxAnimation, x + getWidthCompact() - 11.6f, y + 4.4f, 5f, 5f, value ? HeliosClient.CLICKGUI.getAccentColor() : 0xFF222222, true, 1);

        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 50) {
            Tooltip.tooltip.changeText(description);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (hoveredSetting((int) mouseX, (int) mouseY) && hoveredOverReset(mouseX, mouseY)) {
            value = defaultValue;
            iSettingChange.onSettingChange(this);
            checkBoxAnimation.startFading(value);
        }
        if (hovered((int) mouseX, (int) mouseY) && button == 0) {
            this.value = !value;
            iSettingChange.onSettingChange(this);
            checkBoxAnimation.startFading(value);
        }
    }
    @Override
    public Boolean get() {
        return value;
    }

    @Override
    public void setValue(Boolean value) {
        this.value = value;
    }

    @Override
    public Object saveToFile(List<Object> objectList) {
        return value;
    }

    @Override
    public void loadFromFile(MapReader map) {
        value =  map.getBoolean(getSaveName(),defaultValue);
    }

    public static class Builder extends SettingBuilder<Builder, Boolean, BooleanSetting> {
        ISettingChange ISettingChange;

        public Builder() {
            super(false);
        }

        public Builder onSettingChange(ISettingChange ISettingChange) {
            this.ISettingChange = ISettingChange;
            return this;
        }

        @Override
        public BooleanSetting build() {
            return new BooleanSetting(name, description, ISettingChange, value, shouldRender, defaultValue);
        }
    }
}

