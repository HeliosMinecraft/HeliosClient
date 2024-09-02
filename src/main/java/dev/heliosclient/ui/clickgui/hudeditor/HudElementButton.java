package dev.heliosclient.ui.clickgui.hudeditor;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.HudManager;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.gui.DrawContext;

public class HudElementButton {
    public int count = 0;
    public HudElement hudElement;
    int x, y, width;
    DrawContext drawContext;

    public HudElementButton(HudElement hudElement) {
        this.hudElement = hudElement;
        HudManager.INSTANCE.hudElements.forEach(hudElement1 -> {
            if (hudElement1.getClass().equals(hudElement.getClass())) {
                count++;
            }
        });
    }

    public void render(DrawContext drawContext, boolean collapsed, int x, int y, float delta) {
        if (collapsed) return;
        this.drawContext = drawContext;
        this.x = x;
        this.y = y;
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y, width, 12, 0xFF222222);
        Renderer2D.drawCustomString(FontRenderers.Small_fxfontRenderer, drawContext.getMatrices(), hudElement.name + " [" + count + "]", x + 3, y + 3, ColorManager.INSTANCE.defaultTextColor());
    }

    public void mouseClicked(double mouseX, double mouseY, int button, boolean collapsed) {
        if (collapsed) return;
        if (hovered(mouseX, mouseY) && button == 0) {
            addInstanceToList(hudElement.getClass());
            updateCount();
        }
    }

    public void updateCount() {
        count = 0;
        HudManager.INSTANCE.hudElements.forEach(hudElement1 -> {
            if (hudElement1.getClass().equals(hudElement.getClass())) {
                ++count;
            }
        });
    }

    public boolean hovered(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + 14;
    }

    public <T extends HudElement> void addInstanceToList(Class<? extends T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            HudManager.INSTANCE.addHudElement(instance);
            HudElement lastHudElement =  HudManager.INSTANCE.hudElements.get(HudManager.INSTANCE.hudElements.size() - 1);

            lastHudElement.posX = HudElement.NUMBER_OF_LINES/2 - 1;
            lastHudElement.posY = HudElement.NUMBER_OF_LINES/2 - 1;
            lastHudElement.distanceX = (HeliosClient.MC.getWindow().getScaledWidth() - lastHudElement.width)/2;
            lastHudElement.distanceY =  (HeliosClient.MC.getWindow().getScaledHeight() - lastHudElement.height)/2;
            lastHudElement.x = (HeliosClient.MC.getWindow().getScaledWidth() - lastHudElement.width)/2;
            lastHudElement.y = (HeliosClient.MC.getWindow().getScaledHeight() - lastHudElement.height)/2;

        } catch (Exception e) {
            HeliosClient.LOGGER.error("Error adding hud element instance to list", e);
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
