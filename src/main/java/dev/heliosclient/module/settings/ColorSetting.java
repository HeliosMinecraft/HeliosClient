package dev.heliosclient.module.settings;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.*;

public class ColorSetting extends Setting {
    public int value;
    public int r;
    public int g;
    public int b;
    public int a;
    boolean slidingRed = false;
    boolean slidingGreen = false;
    boolean slidingBlue = false;
    boolean slidingAlpha = false;
    Module_ module;
    int boxSize = 20;

    public ColorSetting(String name, String description, Module_ module, int value) {
        this.name = name;
        this.description = description;
        this.value = value;
        this.height = 80;
        this.heightCompact = 80;
        this.module = module;
        this.a = (value >> 24) & 0xFF;
        this.r = (value >> 16) & 0xFF;
        this.g = (value >> 8) & 0xFF;
        this.b = value & 0xFF;

    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {

        double diff = Math.min(100, Math.max(0, (mouseY - y - 14) / 0.6));

        int colorValue = (int) MathUtils.round(((diff / 100) * 255), 0);
        if (slidingAlpha) {
            a = 255 - colorValue;
            module.onSettingChange(this);
        }
        if (slidingRed) {
            r = 255 - colorValue;
            module.onSettingChange(this);
        }
        if (slidingGreen) {
            g = 255 - colorValue;
            module.onSettingChange(this);
        }
        if (slidingBlue) {
            b = 255 - colorValue;
            module.onSettingChange(this);
        }

        value = new Color(r, g, b, a).getRGB();

        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),name, x + 2, y + 2,256 - ColorUtils.getRed(defaultColor),256 - ColorUtils.getGreen(defaultColor),256 - ColorUtils.getBlue(defaultColor),256 - ColorUtils.getAlpha(defaultColor),10f);


        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),name, x + 2, y + 2,256 - ColorUtils.getRed(0xFFFFFF),256 - ColorUtils.getGreen(0xFFFFFF),256 - ColorUtils.getBlue(0xFFFFFF),256 - ColorUtils.getAlpha(0xFFFFFF),10f);
        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),"Red: " + r, x + 130, y + 15,256 - ColorUtils.getRed(defaultColor),256 - ColorUtils.getGreen(defaultColor),256 - ColorUtils.getBlue(defaultColor),256 - ColorUtils.getAlpha(defaultColor),10f);
        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),"Green: " + g, x + 130, y + 29,256 - ColorUtils.getRed(defaultColor),256 - ColorUtils.getGreen(defaultColor),256 - ColorUtils.getBlue(defaultColor),256 - ColorUtils.getAlpha(defaultColor),10f);
        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),"Blue: " + b, x + 130, y + 43,256 - ColorUtils.getRed(defaultColor),256 - ColorUtils.getGreen(defaultColor),256 - ColorUtils.getBlue(defaultColor),256 - ColorUtils.getAlpha(defaultColor),10f);
        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),"Alpha: " + a, x + 130, y + 57,256 - ColorUtils.getRed(defaultColor),256 - ColorUtils.getGreen(defaultColor),256 - ColorUtils.getBlue(defaultColor),256 - ColorUtils.getAlpha(defaultColor),10f);

        // drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 2, 0xFFFFFF, false);
        //drawContext.drawText(textRenderer, Text.literal("Red: " + r), x + 130, y + 15, defaultColor, false);
        //drawContext.drawText(textRenderer, Text.literal("Green: " + g), x + 130, y + 29, defaultColor, false);
        //drawContext.drawText(textRenderer, Text.literal("Blue: " + b), x + 130, y + 43, defaultColor, false);
        //drawContext.drawText(textRenderer, Text.literal("Alpha: " + a), x + 130, y + 57, defaultColor, false);
        drawContext.fillGradient(x + 65, y + 14, x + 77, y + 74, 0xFFDDDDDD, 0x00DDDDDD);
        drawContext.fillGradient(x + 80, y + 14, x + 92, y + 74, 0xFFFF0000, 0xFF000000);
        drawContext.fillGradient(x + 95, y + 14, x + 107, y + 74, 0xFF00FF00, 0xFF000000);
        drawContext.fillGradient(x + 110, y + 14, x + 122, y + 74, 0xFF0000FF, 0xFF000000);
        int scaledValueAlpha = (int) ((double) (255 - a) / 255 * 60);
        int scaledValueRed = (int) ((double) (255 - r) / 255 * 60);
        int scaledValueGreen = (int) ((double) (255 - g) / 255 * 60);
        int scaledValueBlue = (int) ((double) (255 - b) / 255 * 60);
        Renderer2D.drawRectangle(drawContext, x + 64, y + 13 + scaledValueAlpha, 14, 2, 0xFFAAAAAA);
        Renderer2D.drawRectangle(drawContext, x + 79, y + 13 + scaledValueRed, 14, 2, 0xFFAAAAAA);
        Renderer2D.drawRectangle(drawContext, x + 94, y + 13 + scaledValueGreen, 14, 2, 0xFFAAAAAA);
        Renderer2D.drawRectangle(drawContext, x + 109, y + 13 + scaledValueBlue, 14, 2, 0xFFAAAAAA);
        Renderer2D.drawRectangle(drawContext, x + 2, y + 14, 60, 60, value);

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

        double diff = Math.min(100, Math.max(0, (mouseY - y - 14) / 0.6));

        int colorValue = (int) MathUtils.round(((diff / 100) * 255), 0);
        if (slidingAlpha) {
            a = 255 - colorValue;
            module.onSettingChange(this);
        }
        if (slidingRed) {
            r = 255 - colorValue;
            module.onSettingChange(this);
        }
        if (slidingGreen) {
            g = 255 - colorValue;
            module.onSettingChange(this);
        }
        if (slidingBlue) {
            b = 255 - colorValue;
            module.onSettingChange(this);
        }

        value = new Color(r, g, b, a).getRGB();
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),name, x + 2, y + 2,defaultColor,8f);


        drawContext.fill(x + moduleWidth - boxSize - 2, y + 2, x + moduleWidth - 2, y + boxSize + 2, value);

        drawContext.fillGradient(x + 15, y + 14, x + 27, y + 74, 0xFFDDDDDD, 0x00DDDDDD);
        drawContext.fillGradient(x + 30, y + 14, x + 42, y + 74, 0xFFFF0000, 0xFF000000);
        drawContext.fillGradient(x + 45, y + 14, x + 57, y + 74, 0xFF00FF00, 0xFF000000);
        drawContext.fillGradient(x + 60, y + 14, x + 72, y + 74, 0xFF0000FF, 0xFF000000);
        int scaledValueAlpha = (int) ((double) (255 - a) / 255 * 60);
        int scaledValueRed = (int) ((double) (255 - r) / 255 * 60);
        int scaledValueGreen = (int) ((double) (255 - g) / 255 * 60);
        int scaledValueBlue = (int) ((double) (255 - b) / 255 * 60);
        Renderer2D.drawRectangle(drawContext, x + 14, y + 13 + scaledValueAlpha, 14, 2, 0xFFAAAAAA);
        Renderer2D.drawRectangle(drawContext, x + 29, y + 13 + scaledValueRed, 14, 2, 0xFFAAAAAA);
        Renderer2D.drawRectangle(drawContext, x + 44, y + 13 + scaledValueGreen, 14, 2, 0xFFAAAAAA);
        Renderer2D.drawRectangle(drawContext, x + 59, y + 13 + scaledValueBlue, 14, 2, 0xFFAAAAAA);

        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 150) {
            Tooltip.tooltip.changeText(description);
        }
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    public int getG() {
        return g;
    }

    public int getR() {
        return r;
    }
    /* @Override
    public void mouseClicked(double mouseX, double mouseY, int button)
    {
        if (button != 0) {return;}
        if (hoveredAlpha((int)mouseX, (int)mouseY))
        {
            this.slidingAlpha = true;
        }
        if (hoveredRed((int)mouseX, (int)mouseY))
        {
            this.slidingRed = true;
        }
        if (hoveredGreen((int)mouseX, (int)mouseY))
        {
            this.slidingGreen = true;
        }
        if (hoveredBlue((int)mouseX, (int)mouseY))
        {
            this.slidingBlue = true;
        }
    }*/

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return;
        }
        if (hoveredAlpha((int) mouseX, (int) mouseY)) {
            this.slidingAlpha = true;
        }
        if (hoveredRed((int) mouseX, (int) mouseY)) {
            this.slidingRed = true;
        }
        if (hoveredGreen((int) mouseX, (int) mouseY)) {
            this.slidingGreen = true;
        }
        if (hoveredBlue((int) mouseX, (int) mouseY)) {
            this.slidingBlue = true;
        }
    }

    public boolean hoveredAlpha(int mouseX, int mouseY) {
        if (quickSettings) {
            return mouseX >= x + 15 && mouseX <= x + 27 && mouseY >= y + 14 && mouseY <= y + 74;
        } else {
            return mouseX >= x + 65 && mouseX <= x + 77 && mouseY >= y + 14 && mouseY <= y + 74;
        }
    }

    public boolean hoveredRed(int mouseX, int mouseY) {
        if (quickSettings) {
            return mouseX >= x + 30 && mouseX <= x + 42 && mouseY >= y + 14 && mouseY <= y + 74;
        } else {
            return mouseX >= x + 80 && mouseX <= x + 92 && mouseY >= y + 14 && mouseY <= y + 74;
        }
    }

    public boolean hoveredGreen(int mouseX, int mouseY) {
        if (quickSettings) {
            return mouseX >= x + 45 && mouseX <= x + 57 && mouseY >= y + 14 && mouseY <= y + 74;
        } else {
            return mouseX >= x + 95 && mouseX <= x + 107 && mouseY >= y + 14 && mouseY <= y + 74;
        }
    }

    public boolean hoveredBlue(int mouseX, int mouseY) {
        if (quickSettings) {
            return mouseX >= x + 60 && mouseX <= x + 72 && mouseY >= y + 14 && mouseY <= y + 74;
        } else {
            return mouseX >= x + 110 && mouseX <= x + 122 && mouseY >= y + 14 && mouseY <= y + 74;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        slidingAlpha = false;
        slidingRed = false;
        slidingGreen = false;
        slidingBlue = false;
    }
}
