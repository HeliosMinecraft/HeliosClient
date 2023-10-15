package dev.heliosclient.module.settings;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.animation.EasingType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class BooleanSetting extends Setting {
    public boolean value;
    Module_ module;
    String description;
    AnimationUtils CheckBoxAnimation = new AnimationUtils();

    public BooleanSetting(String name, String description, Module_ module, boolean value) {
        this.module = module;
        this.name = name;
        this.description = description;
        this.heightCompact = 18;
        this.value = value;
        CheckBoxAnimation.FADE_SPEED = 0.1f;
        CheckBoxAnimation.startFading(true, EasingType.QUADRATIC_IN_OUT);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        Renderer2D.drawFixedString(drawContext.getMatrices(),name, x + 2, y + 8,ColorManager.INSTANCE.defaultTextColor());
        //drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor(), false);
        Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x + 175, y + 7, 10, 10, 1, 0xFFFFFFFF);
        CheckBoxAnimation.drawFadingAndPoppingBox(drawContext, x + 177, y + 9, 6, 6, value ? 0xFF55FFFF : 0xFF222222, false, 0);

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
        Renderer2D.drawFixedString(drawContext.getMatrices(),name.substring(0, Math.min(12, name.length())) + "...", x + 3, y + 5,ColorManager.INSTANCE.defaultTextColor());
        //drawContext.drawText(textRenderer, Text.literal(name.substring(0, Math.min(12, name.length())) + "..."), x + 3, y + 5, ColorManager.INSTANCE.defaultTextColor(), false);
        Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x + moduleWidth - 12, y + 4, 10, 10, 1, 0xFFFFFFFF);
        CheckBoxAnimation.drawFadingAndPoppingBox(drawContext, x + moduleWidth - 10, y + 6, 6, 6, value ? 0xAA55FFFF : 0xFF222222, false, 0);

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
        if (hovered((int) mouseX, (int) mouseY) && button == 0) {
            this.value = !value;
            module.onSettingChange(this);
            CheckBoxAnimation.startFading(value, EasingType.QUADRATIC_OUT);
        }
    }
}

