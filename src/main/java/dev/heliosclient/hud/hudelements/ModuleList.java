package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.module.ModuleManager;
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
        this.height = 13;
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
            int nameWidth = mc.textRenderer.getWidth(m.name);
            maxWidth = Math.max(maxWidth, nameWidth);
            totalY += 12;
        }

        Collections.sort(enabledModules, Comparator.comparing(module -> module.name.length(), Comparator.reverseOrder()));

        for (Module_ m : enabledModules) {
            if (!m.showInModulesList.value) continue;
            int nameWidth = mc.textRenderer.getWidth(m.name);
            Renderer2D.drawRectangle(drawContext, x - 4 - width / 2 + width - nameWidth, y - height / 2 + yOffset, nameWidth + 2, 12, 0x66222222);

            Renderer2D.drawRectangle(drawContext, x - 2 - width / 2 + width, y - height / 2, 2, yOffset + 12, HeliosClient.uiColorA);

            drawContext.drawText(mc.textRenderer, m.name, x - 2 + width / 2 - nameWidth, y - height / 2 + yOffset + 2, 0xFFFFFFFF, false);
            yOffset += 12;
        }
        this.width = maxWidth + 5;
        this.height = totalY + 3;
    }

    @SubscribeEvent
    public void update(TickEvent.CLIENT event) {
        enabledModules = ModuleManager.INSTANCE.getEnabledModules();
    }

}
