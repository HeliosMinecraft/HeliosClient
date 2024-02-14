package dev.heliosclient.module.settings;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.system.Config;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.render.Renderer2D;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import static com.mojang.text2speech.Narrator.LOGGER;

public class CycleSetting extends Setting<Integer> {
    public List options;
    public int value;
    Color color = new Color(4, 3, 3, 157);

    public <T> CycleSetting(String name, String description, ISettingChange iSettingChange, List<T> options, int value, BooleanSupplier shouldRender, int defaultValue) {
        super(shouldRender, defaultValue);
        this.name = name;
        this.description = description;
        this.options = options;
        this.height = 24;
        this.heightCompact = 12;
        this.iSettingChange = iSettingChange;
        this.value = value;
    }

    public <T> CycleSetting(String name, String description, ISettingChange iSettingChange, T[] options, int value, BooleanSupplier shouldRender, int defaultValue) {
        super(shouldRender, defaultValue);
        this.name = name;
        this.description = description;
        this.options = Arrays.asList(options);
        this.height = 24;
        this.heightCompact = 12;
        this.iSettingChange = iSettingChange;
        this.value = value;
    }

    public void setOptions(List options) {
        this.options = options;
    }


    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);

        if (options.isEmpty() || options.size() - 1 < value) {
            Renderer2D.drawFixedString(drawContext.getMatrices(), "No option found!", x + 2, y + 4, 0xFFFF0000);
        } else {
            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + Renderer2D.getFxStringWidth(name + ": "), y + 2, Renderer2D.getFxStringWidth(options.get(value).toString()) + 4, Renderer2D.getFxStringHeight(options.get(value).toString()) + 3, 3, color.getRGB());
            Renderer2D.drawFixedString(drawContext.getMatrices(), name + ": " + options.get(value), x + 2, y + 4, ColorManager.INSTANCE.defaultTextColor());
        }

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
            FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), "No option found!", x + 2, y + 2, 0xFFFF0000);
        } else {
            FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), name + ": " + options.get(value).toString().substring(0, Math.min(12, options.get(value).toString().length())) + "...", x + 2, y + 2, ColorManager.INSTANCE.defaultTextColor());
        }

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
        super.mouseClicked(mouseX, mouseY, button);
        if (hoveredSetting((int) mouseX, (int) mouseY) && hoveredOverReset(mouseX, mouseY)) {
            value = defaultValue;
            iSettingChange.onSettingChange(this);
        }
        if (options.isEmpty() || options.size() - 1 < value) {
            return;
        }
        if (hovered((int) mouseX, (int) mouseY) && button == 0) {
            if (value == options.size() - 1) {
                value = 0;
            } else {
                value++;
            }
            iSettingChange.onSettingChange(this);
        } else if (hovered((int) mouseX, (int) mouseY) && button == 1) {
            if (value <= 0) {
                value = options.size() - 1;
            } else {
                value--;
            }
            iSettingChange.onSettingChange(this);
        }
    }

    @Override
    public Object saveToToml(List<Object> objectList) {
        if (options.isEmpty() || options.size() - 1 < value) {
            return null;
        }
        return options.get(value);
    }

    @Override
    public void loadFromToml(Map<String, Object> MAP, Toml toml) {
        super.loadFromToml(MAP,toml);
        if(MAP.get(name.replace(" ", "")) == null){
            value = defaultValue;
            return;
        }
        int optionIndex = options.indexOf(MAP.get(name.replace(" ", "")));
        if (optionIndex != -1) {
            value = optionIndex;
        } else {
            LOGGER.error("Option not found for: " + MAP.get(name.replace(" ", "")) + ", " + name + " Setting during loading config: " + Config.MODULES);
        }
    }

    public static class Builder extends SettingBuilder<Builder, List, CycleSetting> {
        ISettingChange ISettingChange;
        int defaultListIndex;

        public Builder() {
            super(new ArrayList<>());
        }

        public Builder onSettingChange(ISettingChange ISettingChange) {
            this.ISettingChange = ISettingChange;
            return this;
        }

        public Builder defaultListIndex(int defaultListIndex) {
            this.defaultListIndex = defaultListIndex;
            return this;
        }

        @Override
        public CycleSetting build() {
            return new CycleSetting(name, description, ISettingChange, value, defaultListIndex, shouldRender, defaultListIndex);
        }
    }
}
