package dev.heliosclient.hud.hudelements;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class ModuleList extends HudElement implements Listener {

    private ArrayList<Module_> enabledModules = ModuleManager.INSTANCE.getEnabledModules();
    public ModuleList() {
        super(DATA);
        this.width = 50;
        EventManager.register(this);
    }    public static HudElementData<ModuleList> DATA = new HudElementData<>("Module List", "Shows enabled modules", ModuleList::new);

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        int yOffset = 0;
        int totalY = 0;
        int maxWidth = 0;

        // Calculate the maximum width of the module names
        for (Module_ m : ModuleManager.INSTANCE.modules) {
            if (!m.showInModulesList.value) continue;
            int nameWidth = Math.round(Renderer2D.getStringWidth(m.name));
            maxWidth = Math.max(maxWidth, nameWidth);
            totalY += Math.round(Renderer2D.getStringHeight()) + 2;
        }

        Collections.sort(enabledModules, Comparator.comparing(module -> module.name.length(), Comparator.reverseOrder()));
        this.width = maxWidth + 5;
        this.height = totalY + 2;

        super.renderElement(drawContext, textRenderer);

        for (Module_ m : enabledModules) {
            if (!m.showInModulesList.value) continue;
            float nameWidth = Renderer2D.getStringWidth(m.name);
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x - 7 + width - nameWidth, this.y + yOffset, nameWidth + 3, Math.round(Renderer2D.getStringHeight()) + 2, 0x66222222);

            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x - 3 + width, this.y, 2, yOffset + Math.round(Renderer2D.getStringHeight()) + 3, HeliosClient.uiColor);

            Renderer2D.drawString(drawContext.getMatrices(), m.name, x - 5 + width - nameWidth, this.y + 1 + yOffset, ColorUtils.rgbaToInt(255, 255, 255, 255));
            yOffset += Math.round(Renderer2D.getStringHeight()) + 2;
        }
    }

    @SubscribeEvent
    public void update(TickEvent.CLIENT event) {
        enabledModules = ModuleManager.INSTANCE.getEnabledModules();
    }



}
