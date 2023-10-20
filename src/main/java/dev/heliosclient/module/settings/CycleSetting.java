package dev.heliosclient.module.settings;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

public class CycleSetting extends Setting {
    public int value;
    public List options;
    Module_ module;

    public <T> CycleSetting(String name, String description, Module_ module, List<T> options, int value, BooleanSupplier shouldRender) {
        super(shouldRender);
        this.name = name;
        this.description = description;
        this.options = options;
        this.height = 24;
        this.module = module;
        this.value = value;
    }

    public <T> CycleSetting(String name, String description, Module_ module, T[] options, int value, BooleanSupplier shouldRender) {
        super(shouldRender);
        this.name = name;
        this.description = description;
        this.options = Arrays.asList(options);
        this.height = 24;
        this.module = module;
        this.value = value;
    }

    public void setOptions(List options) {
        this.options = options;
    }


    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);

        if (options.isEmpty() || options.size() - 1 < value) {
            Renderer2D.drawFixedString(drawContext.getMatrices(), "No option found!", x + 10, y + 28, 0xFFFF0000);
        }
        Renderer2D.drawFixedString(drawContext.getMatrices(), name + ": " + options.get(value), x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor());

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

        if (options.isEmpty() || options.size() - 1 < value) {
            compactFont.drawString(drawContext.getMatrices(), "No option found!", x + 10, y + 28, 0xFFFF0000);
        }
        compactFont.drawString(drawContext.getMatrices(), name + ": " + options.get(value).toString().substring(0, Math.min(12, options.get(value).toString().length())) + "...", x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor());

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
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (options.isEmpty() || options.size() - 1 < value) {
            return;
        }
        if (hovered((int) mouseX, (int) mouseY) && button == 0) {
            if (value == options.size() - 1) {
                value = 0;
            } else {
                value++;
            }
            module.onSettingChange(this);
        }
    }

    public static class Builder extends SettingBuilder<Builder, List, CycleSetting> {
        Module_ module;
        int listValue;

        public Builder() {
            super(new ArrayList<>());
        }

        public Builder module(Module_ module) {
            this.module = module;
            return this;
        }

        public Builder listValue(int listValue) {
            this.listValue = listValue;
            return this;
        }

        @Override
        public CycleSetting build() {
            return new CycleSetting(name, description, module, value, listValue, shouldRender);
        }
    }
}
