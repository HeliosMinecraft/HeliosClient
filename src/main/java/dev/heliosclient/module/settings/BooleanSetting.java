package dev.heliosclient.module.settings;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class BooleanSetting extends Setting<Boolean> {
    public boolean value;
    String description;
    AnimationUtils CheckBoxAnimation = new AnimationUtils();

    public BooleanSetting(String name, String description, ISettingChange iSettingChange, boolean value, BooleanSupplier shouldRender, boolean defaultValue) {
        super(shouldRender, defaultValue);
        this.iSettingChange = iSettingChange;
        this.name = name;
        this.description = description;
        this.heightCompact = 14;
        this.value = value;
        CheckBoxAnimation.FADE_SPEED = 0.1f;
        CheckBoxAnimation.startFading(true, EasingType.QUADRATIC_IN_OUT);
    }

    public BooleanSetting(String name, String description, ISettingChange iSettingChange, boolean defaultValue) {
        this(name, description, iSettingChange, defaultValue, () -> true, defaultValue);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor());

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x + 175, y + 7, 10, 10, 2, 0.7f, 0xFFFFFFFF);
        CheckBoxAnimation.drawFadingAndPoppingBox(drawContext, x + 176.7f, y + 8.7f, 6.3f, 6.3f, value ? HeliosClient.CLICKGUI.getAccentColor() : 0xFF222222, true, 2);

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

        CheckBoxAnimation.drawFadingAndPoppingBox(drawContext, x + getWidthCompact() - 11.6f, y + 4.4f, 5f, 5f, value ? HeliosClient.CLICKGUI.getAccentColor() : 0xFF222222, true, 1);

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
            CheckBoxAnimation.startFading(value, EasingType.QUADRATIC_OUT);
        }
        if (hovered((int) mouseX, (int) mouseY) && button == 0) {
            this.value = !value;
            iSettingChange.onSettingChange(this);
            CheckBoxAnimation.startFading(value, EasingType.QUADRATIC_OUT);
        }
    }

    @Override
    public Object saveToToml(List<Object> objectList) {
        return value;
    }

    @Override
    public void loadFromToml(Map<String, Object> MAP, Toml toml) {
        super.loadFromToml(MAP, toml);
        if (MAP.get(getSaveName()) == null) {
            value = defaultValue;
            return;
        }
        value = (boolean) MAP.get(getSaveName());
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

