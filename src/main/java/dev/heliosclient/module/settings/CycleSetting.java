package dev.heliosclient.module.settings;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.misc.MapReader;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

import static com.mojang.text2speech.Narrator.LOGGER;


public class CycleSetting extends Setting<Integer> {
    public List<?> options;
    public int value;
    List<String> tooltipText;
    Color color = new Color(4, 3, 3, 157);

    public <T> CycleSetting(String name, String description, ISettingChange iSettingChange, List<T> options, int value, BooleanSupplier shouldRender, int defaultValue, List<String> optionsTooltips) {
        super(shouldRender, defaultValue);
        this.name = name;
        this.description = description;
        this.options = options;
        this.height = 24;
        this.heightCompact = 12;
        this.iSettingChange = iSettingChange;
        this.value = value;
        this.tooltipText = optionsTooltips;
    }

    public <T> CycleSetting(String name, String description, ISettingChange iSettingChange, T[] options, int value, BooleanSupplier shouldRender, int defaultValue, List<String> optionsTooltips) {
        this(name, description, iSettingChange, Arrays.asList(options), value, shouldRender, defaultValue, optionsTooltips);
    }

    public void setOptions(List<?> options) {
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


        // Mouse hovers over the options
        if (mouseHoveringOverOptions(mouseX, mouseY) && !tooltipText.isEmpty()) {
            if (value < options.size()) {
                Tooltip.tooltip.changeText(tooltipText.get(value));
            }
        } else if (hovertimer >= 50) {
            Tooltip.tooltip.changeText(description);
        }
    }

    @Override
    public Integer get() {
        return value;
    }

    public boolean mouseHoveringOverOptions(int mouseX, int mouseY) {
        return mouseX >= x + 2 && mouseX <= x + 2 + Renderer2D.getFxStringWidth(name + ": ") && mouseY >= y + 3 && mouseY <= y + 3 + Renderer2D.getFxStringHeight() + 4;
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
    public void setValue(Integer value) {
        this.value = value;
    }

    public void setOption(Object value) {
       if(options.contains(value)){
           setValue(options.indexOf(value));
       }
    }

    @Override
    public Object saveToFile(List<Object> objectList) {
        if (options.isEmpty() || options.size() - 1 < value) {
            return "";
        }
        return options.get(value);
    }

    @Override
    public void loadFromFile(MapReader map) {
        if (!shouldSaveOrLoad) {
            return;
        }
        String mapGet = map.getString(getSaveName(),null);

        if(mapGet == null || !map.has(getSaveName())){
            value = defaultValue;
            return;
        }
        mapGet = mapGet.trim();

        for (Object object :
                options) {
            if (object.toString().trim().equalsIgnoreCase(mapGet)) {
                value = options.indexOf(object);
                return;
            }
        }
        LOGGER.error("Option not found for: {}, {} Setting during loading config: {}", mapGet, name, HeliosClient.CONFIG.moduleConfigManager.getCurrentConfig().getName());
    }

    public Object getOption() {
        if(value >= 0 && value < options.size()) {
            return options.get(value);
        }

        return null;
    }
    public boolean isOption(Object o) {
        if(value >= 0 && value < options.size()) {
            return options.get(value) == o;
        }

        return false;
    }

    public static class Builder extends SettingBuilder<Builder, List<?>, CycleSetting> {
        ISettingChange ISettingChange;
        int defaultListIndex;
        List<String> optionsTooltips = new ArrayList<>();

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

        public Builder defaultListOption(Object o) {
            if (value == null) {
                throw new NullPointerException("Option List is null, could not add default option");
            }
            if (value.contains(o)) {
                this.defaultListIndex = value.indexOf(o);
            }
            return this;
        }

        /**
         * Option order should be corresponding to value list order
         *
         * @param optionToolTip tooltip for the option index
         * @return Builder
         */
        public Builder addOptionToolTip(String optionToolTip) {
            optionsTooltips.add(optionToolTip);
            return this;
        }

        @Override
        public CycleSetting build() {
            return new CycleSetting(name, description, ISettingChange, value, defaultListIndex, shouldRender, defaultListIndex, optionsTooltips);
        }
    }
}
