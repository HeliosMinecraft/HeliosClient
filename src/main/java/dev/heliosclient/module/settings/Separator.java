package dev.heliosclient.module.settings;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class Separator extends Setting
{
    public boolean value;

    public Separator(int height)
    {
        this.height = height;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        drawContext.fill(x+2, y+height/2, x+width-2, y+height/2+1, 0xCCFFFFFF);
    }

}
