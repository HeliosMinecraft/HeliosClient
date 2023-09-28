package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class ModuleList extends HudElement implements Listener {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private ArrayList<Module_> enabledModules = ModuleManager.INSTANCE.getEnabledModules();

    public ModuleList() {
        super("Module List", "Shows enabled modules");
        this.width = 50;
        EventManager.register(this);
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        int yOffset = 0;
        int totalY = 0;
        int maxWidth = 0;

        // Calculate the maximum width of the module names
        for (Module_ m : ModuleManager.INSTANCE.modules) {
            if (!m.showInModulesList.value) continue;
            int nameWidth = Math.round(FontManager.fontRenderer.getStringWidth(m.name));
            maxWidth = Math.max(maxWidth, nameWidth);
            totalY += Math.round(FontManager.fontRenderer.getStringHeight("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")) + 2;
        }

        Collections.sort(enabledModules, Comparator.comparing(module -> module.name.length(), Comparator.reverseOrder()));
        this.width = maxWidth + 5;
        this.height = totalY + 2;
        for (Module_ m : enabledModules) {
            if (!m.showInModulesList.value) continue;
            float nameWidth = FontManager.fontRenderer.getStringWidth(m.name);
            Renderer2D.drawRectangle(drawContext, x - 4 - (float) width / 2 + width - nameWidth, this.y - (float) height / 2 + yOffset, nameWidth + 2, Math.round(FontManager.fontRenderer.getStringHeight("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")) + 2, 0x66222222);

            Renderer2D.drawRectangle(drawContext, x - 2 - (float) width / 2 + width, this.y - (float) height / 2, 2, yOffset - 1 + Math.round(FontManager.fontRenderer.getStringHeight("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")) + 2, HeliosClient.uiColorA);

            FontManager.fontRenderer.drawString(drawContext.getMatrices(), m.name, x - 2 + width / 2 - nameWidth, this.y + 1 - (float) height / 2 + yOffset, 1,1,1,1);
            //drawContext.drawText(mc.textRenderer, m.name, x - 2 + width / 2 - nameWidth, y - height / 2 + yOffset + 2, 0xFFFFFFFF, false);
            yOffset += Math.round(FontManager.fontRenderer.getStringHeight("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")) + 2;
        }
    }

    @SubscribeEvent
    public void update(TickEvent.CLIENT event) {
        enabledModules = ModuleManager.INSTANCE.getEnabledModules();
    }

}
