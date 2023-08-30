package dev.heliosclient.hud;

import dev.heliosclient.ui.clickgui.hudeditor.HudEditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;

public class HudManager {
    public static HudManager INSTANCE = new HudManager();

    public ArrayList<HudElement> hudElements = new ArrayList<>();

    protected MinecraftClient mc = MinecraftClient.getInstance();

    public void render(DrawContext drawContext, TextRenderer textRenderer) {
        for (HudElement element : hudElements) {
            element.render(drawContext, textRenderer);
        }
    }

    public void renderEditor(DrawContext drawContext, TextRenderer textRenderer, int mouseX, int mouseY) {
        for (HudElement element : hudElements) {
            element.renderEditor(drawContext, textRenderer, mouseX, mouseY);
        }
    }
}
