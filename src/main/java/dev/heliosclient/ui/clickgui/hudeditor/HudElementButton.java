package dev.heliosclient.ui.clickgui.hudeditor;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.HudManager;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.fontutils.FontRenderers;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class HudElementButton {
    public int count = 0;
    public HudElement hudElement;
    int x, y, width;
    DrawContext drawContext;

    public HudElementButton(HudElement hudElement) {
        this.hudElement = hudElement;
    }

    public void render(DrawContext drawContext, boolean collapsed, int x, int y, float delta) {
        if(collapsed)return;
        this.drawContext = drawContext;
        this.x = x;
        this.y = y;
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, width, 12, 0xFF222222);
        Renderer2D.drawCustomString(FontRenderers.Small_fxfontRenderer,drawContext.getMatrices(), hudElement.name + " [" + count + "]", x + 3, y + 3, ColorManager.INSTANCE.defaultTextColor());
    }

    public void mouseClicked(double mouseX, double mouseY, int button, boolean collapsed) {
        if(collapsed)return;
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
            HudManager.INSTANCE.addHudElement(instance);
            HudManager.INSTANCE.hudElements.get(HudManager.INSTANCE.hudElements.size() - 1).posX = 1;
            HudManager.INSTANCE.hudElements.get(HudManager.INSTANCE.hudElements.size() - 1).posY = 1;
            HudManager.INSTANCE.hudElements.get(HudManager.INSTANCE.hudElements.size() - 1).distanceX = 0;
            HudManager.INSTANCE.hudElements.get(HudManager.INSTANCE.hudElements.size() - 1).distanceY = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public HudElement getHudElement() {
        return hudElement;
    }
}
