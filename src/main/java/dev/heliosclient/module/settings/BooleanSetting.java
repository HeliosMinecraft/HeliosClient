package dev.heliosclient.module.settings;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.animation.EasingType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.BooleanSupplier;

public class BooleanSetting extends Setting<Boolean> {
    Module_ module;
    String description;
    AnimationUtils CheckBoxAnimation = new AnimationUtils();

    public BooleanSetting(String name, String description, Module_ module, boolean value, BooleanSupplier shouldRender, boolean defaultValue) {
        super(shouldRender, defaultValue);
        this.module = module;
        this.name = name;
        this.description = description;
        this.heightCompact = 14;
        this.value = value;
        CheckBoxAnimation.FADE_SPEED = 0.1f;
        CheckBoxAnimation.startFading(true, EasingType.QUADRATIC_IN_OUT);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor());

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x + 175, y + 7, 10, 10, 2, 0.7f, 0xFFFFFFFF);
        CheckBoxAnimation.drawFadingAndPoppingBox(drawContext, x + 176.7f, y + 8.7f, 6.3f, 6.3f, value ? 0xFF55FFFF : 0xFF222222, true, 2);

        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 150) {
            Tooltip.tooltip.changeText(description);
        }
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);

        compactFont.drawString(drawContext.getMatrices(), name, x + 3, y + 4, ColorManager.INSTANCE.defaultTextColor());

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x + widthCompact - 12, y + 4, 7, 7, 2, 0.6f, 0xFFFFFFFF);

        CheckBoxAnimation.drawFadingAndPoppingBox(drawContext, x + widthCompact - 11.6f, y + 4.4f, 5f, 5f, value ? 0xAA55FFFF : 0xFF222222, true, 1);

        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 100) {
            Tooltip.tooltip.changeText(description);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredSetting((int) mouseX, (int) mouseY) && hoveredOverReset(mouseX, mouseY)) {
            value = defaultValue;
            module.onSettingChange(this);
            CheckBoxAnimation.startFading(value, EasingType.QUADRATIC_OUT);
        }
        if (hovered((int) mouseX, (int) mouseY) && button == 0) {
            this.value = !value;
            module.onSettingChange(this);
            CheckBoxAnimation.startFading(value, EasingType.QUADRATIC_OUT);
        }
    }

    public static class Builder extends SettingBuilder<Builder, Boolean, BooleanSetting> {
        Module_ module;

        public Builder() {
            super(false);
        }

        public Builder module(Module_ module) {
            this.module = module;
            return this;
        }

        @Override
        public BooleanSetting build() {
            return new BooleanSetting(name, description, module, value, shouldRender, defaultValue);
        }
    }
}

