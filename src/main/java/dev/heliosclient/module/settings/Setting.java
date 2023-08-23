package dev.heliosclient.module.settings;

import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public abstract class Setting
{
    public String name;
    public int height = 24;
    public int width = 192;
    public Object value;
    private int hoverAnimationTimer = 0;
    public boolean quickSettings=false;
    protected int moduleWidth=96;


    int x = 0, y = 0;
    int hovertimer = 0;
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer)
    {
        this.x = x;
        this.y = y;
        if(hovered(mouseX, mouseY)) {
            hoverAnimationTimer = Math.min(hoverAnimationTimer+1, 40);
        } else {
            hoverAnimationTimer = Math.max(hoverAnimationTimer-1, 0);
        }
        if (quickSettings){
            width=96;
        }
        else {
            width=192;
        }

        int fillColor;
        if(quickSettings) {
            fillColor = (int) (40 + 0.85 * hoverAnimationTimer);
            Renderer2D.drawRoundedRectangle(drawContext, x, y, width, height, 2, new Color(fillColor, fillColor, fillColor, 255).getRGB());
        } else {
            fillColor = (int) (34 + 0.85 * hoverAnimationTimer);
            Renderer2D.drawRoundedRectangle(drawContext, x, y, width, height, 2, new Color(fillColor, fillColor, fillColor, 255).getRGB());
        }
    }

	public void mouseClicked(double mouseX, double mouseY, int button) { }
	
	public void mouseReleased(double mouseX, double mouseY, int button) { }

    public void keyPressed(int keyCode, int scanCode, int modifiers) { }

    protected boolean hovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + 192 && mouseY >= y && mouseY <= y + height;
    }
}
