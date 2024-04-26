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
import dev.heliosclient.util.TimerUtils;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class ModuleList extends HudElement implements Listener {

    private ArrayList<Module_> enabledModules = ModuleManager.INSTANCE.getEnabledModules();
    private Color rainbow = new Color(255, 255, 255);
    private double rainbowHue1;
    private double rainbowHue2;

    public ModuleList() {
        super(DATA);
        this.width = 50;
        EventManager.register(this);
    }

    public static HudElementData<ModuleList> DATA = new HudElementData<>("Module List", "Shows enabled modules", ModuleList::new);

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        int maxWidth = 0;

        // Calculate the maximum width of the module names for enabled modules only
        for (Module_ m : enabledModules) {
            if (!m.showInModulesList.value) continue;
            int nameWidth = Math.round(Renderer2D.getStringWidth(m.name));
            maxWidth = Math.max(maxWidth, nameWidth);
        }
        rainbowHue1 += 0.01f * mc.getTickDelta();
        if (rainbowHue1 > 1) rainbowHue1 -= 1;
        else if (rainbowHue1 < -1) rainbowHue1 += 1;

        rainbowHue2 = rainbowHue1;

        // Render each module with a different color
        this.width = maxWidth + 5;
        int yOffset = this.y; // Start rendering from this.y
        for (Module_ m : enabledModules) {
            if (!m.showInModulesList.value) continue;

            float nameWidth = Renderer2D.getStringWidth(m.name);

            // Draw a background rectangle for each module
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(),
                    x - 6 + width - nameWidth, yOffset, nameWidth + 3,
                    Math.round(Renderer2D.getStringHeight()) + 2, 0x66222222);

            // Draw a vertical separator line
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(),
                    x - 2 + width, yOffset, 2,
                    Math.round(Renderer2D.getStringHeight()) + 3, HeliosClient.uiColor);

            rainbowHue2 += 2f;
            rainbow = new Color(Color.HSBtoRGB((float) rainbowHue2, 1f, 1f));
            // Draw the module name
            Renderer2D.drawString(drawContext.getMatrices(), m.name,
                    x - 4 + width - nameWidth, 1 + yOffset,
                    rainbow.getRGB());

            yOffset += Math.round(Renderer2D.getStringHeight()) + 2;
        }
        this.height =Math.max(yOffset - this.y + 2,40);
    }

    @SubscribeEvent
    public void update(TickEvent.CLIENT event) {
        enabledModules = ModuleManager.INSTANCE.getEnabledModules();
        enabledModules.sort(Comparator.comparing(module -> Renderer2D.getStringWidth(module.name), Comparator.reverseOrder()));
    }
}
