package dev.heliosclient.ui.clickgui.navbar;

import dev.heliosclient.managers.NavBarManager;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class NavBar {
    public static final NavBar navBar = new NavBar();

    public NavBar() {
    }

    public void render(DrawContext drawContext, TextRenderer textRenderer, int mouseX, int mouseY) {
        int width = 2;
        for (NavBarItem item : NavBarManager.INSTANCE.navBarItems) {
            width += Math.round(Renderer2D.getFxStringWidth(item.name)) + 4;
        }
        int x = drawContext.getScaledWindowWidth() / 2 - width / 2;
        int textX = x + 4;
        for (NavBarItem item : NavBarManager.INSTANCE.navBarItems) {
            item.render(drawContext, textRenderer, textX, 0, mouseX, mouseY, NavBarManager.INSTANCE.navBarItems.indexOf(item) == 0, NavBarManager.INSTANCE.navBarItems.size() == NavBarManager.INSTANCE.navBarItems.indexOf(item) + 1);
            textX += item.width;
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        for (NavBarItem item : NavBarManager.INSTANCE.navBarItems) {
            item.mouseClicked(mouseX, mouseY, button);
        }
    }
}
