package dev.heliosclient.ui.clickgui.hudeditor;

import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.KeyPressedEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementList;
import dev.heliosclient.hud.HudManager;
import dev.heliosclient.module.modules.ClickGUI;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class HudEditorScreen extends Screen implements Listener {
    public HudCategoryPane pane = new HudCategoryPane();

    public HudEditorScreen() {
        super(Text.of("Hud editor"));
        EventManager.register(this);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);
        HudManager.INSTANCE.renderEditor(drawContext, textRenderer, mouseX, mouseY);
        HudCategoryPane.INSTACE.render(drawContext, textRenderer, mouseX, mouseY, delta);
        NavBar.navBar.render(drawContext, textRenderer, mouseX, mouseY);
    }

    @Override
    public boolean shouldPause() {
        return ClickGUI.pause;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        NavBar.navBar.mouseClicked((int) mouseX, (int) mouseY, button);
        HudCategoryPane.INSTACE.mouseClicked(mouseX, mouseY,button);

        if (button == 0) {
        int lastHoveredIndex = -1;
        for (int i = HudManager.INSTANCE.hudElements.size()  - 1; i >= 0; i--) {
            if (HudManager.INSTANCE.hudElements.get(i).hovered(mouseX, mouseY)) {
                HudManager.INSTANCE.hudElements.get(i).selected = true;
                lastHoveredIndex = i;
                break;
            }
        }

        for (int i = 0; i < HudManager.INSTANCE.hudElements.size(); i++) {
            if (i != lastHoveredIndex) {
                HudManager.INSTANCE.hudElements.get(i).selected = false;
            }
                HudManager.INSTANCE.hudElements.get(i).mouseClicked(mouseX, mouseY, button);
        }
        }
        return super.mouseClicked(mouseX,mouseY,button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        HudCategoryPane.INSTACE.mouseReleased(mouseX, mouseY,button);
        for (HudElement element : HudManager.INSTANCE.hudElements) {
            element.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX,mouseY,button);
    }

    @SubscribeEvent
    public void keyPressed(KeyPressedEvent keyPressedEvent) {
        if (keyPressedEvent.getKey() == GLFW.GLFW_KEY_DELETE || keyPressedEvent.getKey() == GLFW.GLFW_KEY_BACKSPACE) {
            for (int i = 0; i < HudManager.INSTANCE.hudElements.size(); i++) {
                if (HudManager.INSTANCE.hudElements.get(i).selected) {
                    HudManager.INSTANCE.hudElements.remove(i);
                    i--;
                }
            }
        }
    }

}
