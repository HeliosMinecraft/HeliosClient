package dev.heliosclient.module.settings;

import dev.heliosclient.module.Module_;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.MathUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.*;

public class ColorSetting extends Setting
{
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
    

    public ColorSetting(String name, String description, Module_ module, int value)
    {
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

    int boxSize = 20;
    int sliderWidth = 10;
    int sliderHeight = boxSize;
    int sliderSpacing = 2;

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

        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 2, ColorManager.INSTANCE.defaultTextColor(), false);


            drawContext.drawText(textRenderer, Text.literal(name), x+2, y+2, 0xFFFFFF, false);
            drawContext.drawText(textRenderer, Text.literal("Red: " + r), x+130, y+15, ColorManager.INSTANCE.defaultTextColor(), false);
            drawContext.drawText(textRenderer, Text.literal("Green: " + g), x+130, y+29, ColorManager.INSTANCE.defaultTextColor(), false);
            drawContext.drawText(textRenderer, Text.literal("Blue: " + b), x+130, y+43, ColorManager.INSTANCE.defaultTextColor(), false);
            drawContext.drawText(textRenderer, Text.literal("Alpha: " + a), x+130, y+57, ColorManager.INSTANCE.defaultTextColor(), false);
            drawContext.fillGradient(x + 65, y + 14, x + 77, y + 74, 0xFFDDDDDD, 0x00DDDDDD);
            drawContext.fillGradient(x + 80, y + 14, x + 92, y + 74, 0xFFFF0000, 0xFF000000);
            drawContext.fillGradient(x + 95, y + 14, x + 107, y + 74, 0xFF00FF00, 0xFF000000);
            drawContext.fillGradient(x + 110, y + 14, x + 122, y + 74, 0xFF0000FF, 0xFF000000);
            int scaledValueAlpha = (int)((double)(255-a) / 255 * 60);
            int scaledValueRed = (int)((double)(255-r) / 255 * 60);
            int scaledValueGreen = (int)((double)(255-g) / 255 * 60);
            int scaledValueBlue = (int)((double)(255-b) / 255 * 60);
            drawContext.fill(x+64, y+13+scaledValueAlpha, x+78, y+15+scaledValueAlpha, 0xFFAAAAAA);
            drawContext.fill(x+79, y+13+scaledValueRed, x+93, y+15+scaledValueRed, 0xFFAAAAAA);
            drawContext.fill(x+94, y+13+scaledValueGreen, x+108, y+15+scaledValueGreen, 0xFFAAAAAA);
            drawContext.fill(x+109, y+13+scaledValueBlue, x+123, y+15+scaledValueBlue, 0xFFAAAAAA);
            drawContext.fill(x+2, y+14, x+62, y+74, value);

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

        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
        drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 2, ColorManager.INSTANCE.defaultTextColor(), false);


            drawContext.fill(x + moduleWidth - boxSize - 2, y + 2, x + moduleWidth - 2, y + boxSize + 2, value);

            drawContext.fillGradient(x + 15, y + 14, x + 27, y + 74, 0xFFDDDDDD, 0x00DDDDDD);
            drawContext.fillGradient(x + 30, y + 14, x + 42, y + 74, 0xFFFF0000, 0xFF000000);
            drawContext.fillGradient(x + 45, y + 14, x + 57, y + 74, 0xFF00FF00, 0xFF000000);
            drawContext.fillGradient(x + 60, y + 14, x + 72, y + 74, 0xFF0000FF, 0xFF000000);
            int scaledValueAlpha = (int)((double)(255-a) / 255 * 60);
            int scaledValueRed = (int)((double)(255-r) / 255 * 60);
            int scaledValueGreen = (int)((double)(255-g) / 255 * 60);
            int scaledValueBlue = (int)((double)(255-b) / 255 * 60);
            drawContext.fill(x+14, y+13+scaledValueAlpha, x+28, y+15+scaledValueAlpha, 0xFFAAAAAA);
            drawContext.fill(x+29, y+13+scaledValueRed, x+43, y+15+scaledValueRed, 0xFFAAAAAA);
            drawContext.fill(x+44, y+13+scaledValueGreen, x+58, y+15+scaledValueGreen, 0xFFAAAAAA);
            drawContext.fill(x+59, y+13+scaledValueBlue, x+73, y+15+scaledValueBlue, 0xFFAAAAAA);

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
            return mouseX >= x + 15 && mouseX <= x + 27 && mouseY >= y+14 && mouseY <= y + 74;
        } else {
            return mouseX >= x + 65 && mouseX <= x + 77 && mouseY >= y+14 && mouseY <= y + 74;
        }
    }

    public boolean hoveredRed(int mouseX, int mouseY) {
        if (quickSettings) {
            return mouseX >= x + 30 && mouseX <= x + 42 && mouseY >= y+14 && mouseY <= y + 74;
        } else {
            return mouseX >= x + 80 && mouseX <= x + 92 && mouseY >= y+14 && mouseY <= y + 74;
        }
    }

    public boolean hoveredGreen(int mouseX, int mouseY) {
        if (quickSettings) {
            return mouseX >= x + 45 && mouseX <= x + 57 && mouseY >= y+14 && mouseY <= y + 74;
        } else {
            return mouseX >= x + 95 && mouseX <= x + 107 && mouseY >= y+14 && mouseY <= y + 74;
        }
    }

    public boolean hoveredBlue(int mouseX, int mouseY) {
        if (quickSettings) {
            return mouseX >= x + 60 && mouseX <= x + 72 && mouseY >= y+14 && mouseY <= y + 74;
        } else {
            return mouseX >= x + 110 && mouseX <= x + 122 && mouseY >= y+14 && mouseY <= y + 74;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button)
    {
        slidingAlpha = false;
        slidingRed = false;
        slidingGreen = false;
        slidingBlue = false;
    }
}
