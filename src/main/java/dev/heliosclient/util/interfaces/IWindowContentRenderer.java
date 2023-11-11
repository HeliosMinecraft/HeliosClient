package dev.heliosclient.util.interfaces;

import dev.heliosclient.ui.clickgui.gui.Window;
import net.minecraft.client.gui.DrawContext;

public interface IWindowContentRenderer {
    void renderContent(Window window, DrawContext drawContext, int x, int y, int mouseX, int mouseY);
}