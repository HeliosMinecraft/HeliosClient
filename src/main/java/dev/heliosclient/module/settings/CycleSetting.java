package dev.heliosclient.module.settings;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.HashMap;

public class CycleSetting extends Setting {
    public int value;
    public ArrayList<String> options;
    Module_ module;

    public CycleSetting(String name, String description, Module_ module, ArrayList<String> options, int value) {
        this.name = name;
        this.description = description;
        this.options = options;
        this.height = 24;
        this.module = module;
        this.value = value;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);

        if (options.isEmpty() || options.size() - 1 < value) {
            FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),"No option found!", x + 10, y + 28, 0xFFFF0000,10f);
        }
        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),name + ": " + options.get(value), x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor(),10f);

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
          //  drawContext.drawText(textRenderer, "No option found!", x + 10, y + 28, 0xFFFF0000, false);
            FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),"No option found!", x + 10, y + 28, 0xFFFF0000,10f);
        }
        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),name + ": " + options.get(value).substring(0, Math.min(12, options.get(value).length())) + "...", x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor(),10f);
        //drawContext.drawText(textRenderer, name + ": " + options.get(value).substring(0, Math.min(12, options.get(value).length())) + "...", x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor(), false);

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
}
