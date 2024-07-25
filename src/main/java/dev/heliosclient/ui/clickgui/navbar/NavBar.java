package dev.heliosclient.ui.clickgui.navbar;

import dev.heliosclient.managers.NavBarManager;
import dev.heliosclient.util.render.Renderer2D;
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
        int i;
        for (i = 0; i < NavBarManager.INSTANCE.navBarItems.size(); i++) {
            NavBarItem item = NavBarManager.INSTANCE.navBarItems.get(i);
            item.render(drawContext, textX, 0, mouseX, mouseY, i == 0, NavBarManager.INSTANCE.navBarItems.size() - 1 == i);
            textX += item.width;
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        for (NavBarItem item : NavBarManager.INSTANCE.navBarItems) {
            item.mouseClicked(mouseX, mouseY, button);
        }
    }
}
