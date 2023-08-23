package dev.heliosclient.module.test;

import dev.heliosclient.HeliosClient;
import net.minecraft.client.gui.DrawContext;

// BooleanSetting class
public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
        width = 80;
        height = 20;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y) {
        drawContext.drawText(HeliosClient.MC.textRenderer, name, x + 2, y + 2, value ? 0x00FF00 : 0xFF0000, false);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            value = !value;
        }
    }
}
