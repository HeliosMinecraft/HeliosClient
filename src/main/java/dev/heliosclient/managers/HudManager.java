package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.RenderEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.ui.clickgui.gui.Quadtree;
import dev.heliosclient.ui.clickgui.hudeditor.HudEditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;

public class HudManager implements Listener {
    public static HudManager INSTANCE = new HudManager();
    public ArrayList<HudElement> hudElements = new ArrayList<>();
    protected MinecraftClient mc = MinecraftClient.getInstance();

    public HudManager() {
        EventManager.register(this);
    }

    @SubscribeEvent
    public void render(RenderEvent event) {
        if (ModuleManager.INSTANCE.getModuleByName("HUD").active.value && !(mc.currentScreen instanceof HudEditorScreen)) {
            for (HudElement element : hudElements) {
                element.render(event.getDrawContext(), HeliosClient.MC.textRenderer);
            }
        }
    }

    public void renderEditor(DrawContext drawContext, TextRenderer textRenderer, int mouseX, int mouseY) {
        for (HudElement element : hudElements) {
            element.renderEditor(drawContext, textRenderer, mouseX, mouseY);
        }
    }

    public void addHudElement(HudElement element) {
        this.hudElements.add(element);
    }

    public void removeHudElement(HudElement element) {
        this.hudElements.remove(element);
    }
}
