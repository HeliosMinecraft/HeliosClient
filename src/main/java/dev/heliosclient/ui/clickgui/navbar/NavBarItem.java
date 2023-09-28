package dev.heliosclient.ui.clickgui.navbar;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public class NavBarItem {
    public String name;
    public String description;
    public Screen target;
    public Boolean selected = false;
    public int width;
    public int x, y;
    public int height = 12;

    public NavBarItem(String name, String description, Screen target) {
        this.name = name;
        this.description = description;
        this.target = target;
    }

    public void render(DrawContext drawContext, TextRenderer textRenderer, int renderX, int renderY, int mouseX, int mouseY, boolean first, boolean last) {
        this.width =Math.round(FontManager.fxfontRenderer.getStringWidth(this.name)) + 4;
        this.x = renderX;
        this.y = renderY;
        Renderer2D.drawRoundedRectangle(drawContext, renderX, renderY, false, false, first, last, width, 12, 3, hovered(mouseX, mouseY) ? 0xFF333333 : 0xFF222222);
        float textHeight = FontManager.fxfontRenderer.getStringHeight("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
        float textY = renderY + (12 - textHeight) / 2; // Center the text vertically
        float textX = renderX + (float) (2 - width) /2;
        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),this.name, renderX + 2, textY, MinecraftClient.getInstance().currentScreen == this.target ? ColorManager.INSTANCE.clickGuiSecondary() : ColorManager.INSTANCE.defaultTextColor(),10);
        //drawContext.drawText(textRenderer, this.name, renderX + 2, renderY + 2, MinecraftClient.getInstance().currentScreen == this.target ? ColorManager.INSTANCE.clickGuiSecondary() : ColorManager.INSTANCE.defaultTextColor(), false);
    }

    public boolean hovered(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (hovered(mouseX, mouseY) && button == 0) {
            MinecraftClient.getInstance().setScreen(target);
        }
    }
}
