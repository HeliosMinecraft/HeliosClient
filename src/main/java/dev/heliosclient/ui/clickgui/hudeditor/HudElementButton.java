package dev.heliosclient.ui.clickgui.hudeditor;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementList;
import dev.heliosclient.hud.HudManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;

public class HudElementButton {
    int x, y;

    DrawContext drawContext;
    int count = 0;
    public HudElement hudElement;

    public HudElementButton(HudElement hudElement) {
        this.hudElement = hudElement;
    }

    public void render(DrawContext drawContext, TextRenderer textRenderer, int x, int y, float delta) {
        this.drawContext=drawContext;
        this.x = x;
        this.y = y;
        Renderer2D.drawRectangle(drawContext, x, y, 96, 14, 0xFF222222);
        drawContext.drawText(textRenderer, hudElement.name + " ["+count+"]", x+3, y+3, ColorManager.INSTANCE.defaultTextColor(), false);
    }

    public void  mouseClicked(double mouseX, double mouseY, int button) {

        if (hovered(mouseX, mouseY) && button == 0) {
            addInstanceToList(hudElement.getClass());
            count++;
        }
    }

    public boolean hovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + 96 && mouseY > y && mouseY < y + 14;
    }

    public <T extends HudElement> void addInstanceToList(Class<? extends T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            HudManager.INSTANCE.hudElements.add(instance);
            HudManager.INSTANCE.hudElements.get(HudManager.INSTANCE.hudElements.size()-1).posX = 1;
            HudManager.INSTANCE.hudElements.get(HudManager.INSTANCE.hudElements.size()-1).posY = 1;
            HudManager.INSTANCE.hudElements.get(HudManager.INSTANCE.hudElements.size()-1).distanceX = 0;
            HudManager.INSTANCE.hudElements.get(HudManager.INSTANCE.hudElements.size()-1).distanceY = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
