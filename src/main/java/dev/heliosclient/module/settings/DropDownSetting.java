package dev.heliosclient.module.settings;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.system.Config;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.Renderer2D;
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

public class DropDownSetting extends Setting<Integer> {
    public List options;
    public int value;
    ISettingChange iSettingChange;
    Color color  = new Color(4, 3, 3, 157);
    int maxOptionWidth = 0;
    public boolean selecting = false;

    public <T> DropDownSetting(String name, String description, ISettingChange iSettingChange, List<T> options, int value, BooleanSupplier shouldRender, int defaultValue) {
        super(shouldRender, defaultValue);
        this.name = name;
        this.description = description;
        this.options = options;
        this.height = 24;
        this.heightCompact = 12;
        this.iSettingChange = iSettingChange;
        this.value = value;
        for (Object option: options){
            maxOptionWidth = Math.max(maxOptionWidth,Math.round(Renderer2D.getFxStringWidth(option.toString())));
        }
    }

    public <T> DropDownSetting(String name, String description, ISettingChange iSettingChange, T[] options, int value, BooleanSupplier shouldRender, int defaultValue) {
        super(shouldRender, defaultValue);
        this.name = name;
        this.description = description;
        this.options = Arrays.asList(options);
        this.height = 24;
        this.heightCompact = 12;
        this.iSettingChange = iSettingChange;
        this.value = value;
        for (Object option: options){
            maxOptionWidth = Math.max(maxOptionWidth,Math.round(Renderer2D.getFxStringWidth(option.toString())));
        }
    }

    public void setOptions(List options) {
        this.options = options;
        for (Object option: options){
            maxOptionWidth = Math.max(maxOptionWidth,Math.round(Renderer2D.getFxStringWidth(option.toString())));
        }
    }


    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        for (Object option: options){
            maxOptionWidth = Math.max(maxOptionWidth,Math.round(Renderer2D.getFxStringWidth(option.toString())));
        }

        if (options.isEmpty() || options.size() - 1 < value) {
            Renderer2D.drawFixedString(drawContext.getMatrices(), "No option found!", x + 2, y + 4, 0xFFFF0000);
        }
        else {
            if(!selecting){
                    this.height = 24;
                    Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + Renderer2D.getFxStringWidth(name + ": ") , y + 2f, Renderer2D.getFxStringWidth(options.get(value).toString()) + 4,Renderer2D.getFxStringHeight() + 2,3, color.darker().darker().getRGB());
                    FontRenderers.Mid_iconRenderer.drawString(drawContext.getMatrices(), "\uF11C", x + Renderer2D.getFxStringWidth(name + ": " + options.get(value)) + 5, y + 4, ColorManager.INSTANCE.defaultTextColor());
            }
            if(selecting) {
                Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(),x + Renderer2D.getFxStringWidth(name + ": "), y + 2f,maxOptionWidth + 4, this.height - 2.0f, 3, color.brighter().getRGB());
                float offset = y + 5.0f + Renderer2D.getFxStringHeight();
                for (Object option : options) {
                    if (option == options.get(value)) {
                        continue;
                    }
                    float x2 = Renderer2D.getFxStringWidth(name + ": ") + x;
                    int center = Math.round(((maxOptionWidth + 4.0f)/2.0f) -  (Renderer2D.getFxStringWidth(option.toString())/2.0f)); // Center the text horizontally
                    Renderer2D.drawHorizontalLine(drawContext.getMatrices().peek().getPositionMatrix(),x2, maxOptionWidth + 4.0f, offset - 0.1f,0.5f, Color.white.getRGB());
                    Renderer2D.drawFixedString(drawContext.getMatrices(), String.valueOf(option), x2 + center, offset, Color.white.getRGB());
                    offset += Renderer2D.getFxStringHeight() + 2.0f;
                }
                this.height = Math.round(offset - y);
            }
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
        }
        else {
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
            selecting = false;
        }
        if (options.isEmpty() || options.size() - 1 < value) {
            return;
        }

        if(selecting) {
            float offset = y + 4.0f + Renderer2D.getFxStringHeight() + 2.0f;
            for (Object option : options) {
                if (option == options.get(value)) {
                    continue;
                }
                if (mouseX >= Renderer2D.getFxStringWidth(name + ": ") + x + 2 && mouseX <= Renderer2D.getFxStringWidth(name + ": ") + x + 2 + Renderer2D.getFxStringWidth(String.valueOf(option)) && mouseY >= offset && mouseY <= offset + Renderer2D.getFxStringHeight() + 2.0f) {
                    value = options.indexOf(option);
                    selecting = false;
                    iSettingChange.onSettingChange(this);
                    break;
                }
                offset += Renderer2D.getFxStringHeight() + 2.0f;
            }
        }

        if (mouseX >= Renderer2D.getFxStringWidth(name + ": ") + x + 2 && mouseX <= Renderer2D.getFxStringWidth(name + ": ") + x + 2 + Renderer2D.getFxStringWidth(options.get(value).toString()) && mouseY >= y +2  && mouseY <= y + Renderer2D.getFxStringHeight() + 2 ) {
            selecting = true;
        }
    }
    @Override
    public Object saveToToml(List<Object> objectList) {
        if(options.isEmpty() || options.size() - 1 < value) {
            return null;
        }
        return options.get(value);
    }

    @Override
    public void loadFromToml(Map<String, Object> MAP, Toml toml) {
        int optionIndex = options.indexOf(MAP.get(name.replace(" ","")));
        if(optionIndex != -1) {
            value = optionIndex;
        } else{
            LOGGER.warn("Option not found for: " + MAP.get(name.replace(" ","")) +", " + name + " Setting during loading config "+ Config.MODULES);
        }
    }

    public static class Builder extends SettingBuilder<Builder, List, DropDownSetting> {
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
        public DropDownSetting build() {
            return new DropDownSetting(name, description, ISettingChange, value, defaultListIndex, shouldRender, defaultListIndex);
        }
    }
}
