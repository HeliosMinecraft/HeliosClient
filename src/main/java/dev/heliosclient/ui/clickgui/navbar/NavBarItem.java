package dev.heliosclient.ui.clickgui.navbar;

import dev.heliosclient.managers.ColorManager;
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
        this.width = Math.round(Renderer2D.getFxStringWidth(this.name)) + 4;
        this.x = renderX;
        this.y = renderY;
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), renderX, renderY, false, false, first, last, width, 12, 3, hovered(mouseX, mouseY) ? 0xFF333333 : 0xFF222222);
        float textHeight = Renderer2D.getFxStringHeight();
        float textY = renderY + (13 - textHeight) / 2; // Center the text vertically
        Renderer2D.drawFixedString(drawContext.getMatrices(), this.name, renderX + 2, textY, MinecraftClient.getInstance().currentScreen == this.target ? ColorManager.INSTANCE.clickGuiSecondary() : ColorManager.INSTANCE.defaultTextColor());
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
