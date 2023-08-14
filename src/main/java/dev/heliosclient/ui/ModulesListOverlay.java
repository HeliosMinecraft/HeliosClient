package dev.heliosclient.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


import dev.heliosclient.module.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.HeliosClient;
import net.minecraft.client.MinecraftClient;

import net.minecraft.client.gui.DrawContext;

public class ModulesListOverlay
{
    public static ModulesListOverlay INSTANCE = new ModulesListOverlay();
    private MinecraftClient mc = MinecraftClient.getInstance();
    private ArrayList<Module_> enabledModules = ModuleManager.INSTANCE.getEnabledModules();

    public void render(DrawContext drawContext, int scaledWidth, int scaledHeight)
    {
        // do not draw if F3 enabled
        if (mc.options.debugEnabled) return;

        int yOffset = 0;

        // I have no idea what is happening here. Please dont touch :(
        Collections.sort(enabledModules, Comparator.comparing(module -> module.name.length(), Comparator.reverseOrder()));

        for (Module_ m : enabledModules)
        {
            if (!m.showInModulesList.value) continue;
            int nameWidth = mc.textRenderer.getWidth(m.name);
            drawContext.fill(scaledWidth - nameWidth - 8, yOffset, scaledWidth, yOffset + 12, 0x55222222);
            drawContext.fill(scaledWidth - 2, yOffset, scaledWidth, yOffset + 12, HeliosClient.uiColorA);
            drawContext.drawText(mc.textRenderer, m.name, scaledWidth - nameWidth - 4, yOffset + 2, 0xFFFFFFFF, false);
            yOffset += 12;
        }
    }

    public void update()
    {
        enabledModules = ModuleManager.INSTANCE.getEnabledModules();
    }
}
