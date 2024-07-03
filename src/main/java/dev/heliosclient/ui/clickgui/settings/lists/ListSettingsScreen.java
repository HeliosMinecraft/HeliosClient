package dev.heliosclient.ui.clickgui.settings.lists;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.module.settings.lists.ListSetting;
import dev.heliosclient.ui.clickgui.SearchBar;
import dev.heliosclient.ui.clickgui.gui.Window;
import dev.heliosclient.util.interfaces.IWindowContentRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ListSettingsScreen extends Screen implements IWindowContentRenderer {
    private final Window window;
    private final ListSetting<?> setting;
    private final SearchBar searchBar;
    int windowWidth = 258, windowHeight = 140;

    public ListSettingsScreen(ListSetting<?> setting) {
        super(Text.of(setting.name));
        this.setting = setting;
        window = new Window(windowHeight, windowWidth, true, this);
        searchBar = new SearchBar();
        EventManager.unregister(searchBar);
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        EventManager.register(searchBar);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        window.render(context, mouseX, mouseY, setting.name, setting.description, textRenderer);
        setting.setSearchTerm(searchBar.getValue());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        window.setBackButtonTask(() -> {
            if (setting.getParentScreen() != null) {
                HeliosClient.MC.setScreen(setting.getParentScreen());
            }
        });

        setting.handleMouseClick(mouseX, mouseY, button, window);

        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        window.mouseScrolled(mouseX, mouseY, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchBar.isFocused()) {
            setting.setHeight(25);
            if (setting.getParentScreen() != null) {
                HeliosClient.MC.setScreen(setting.getParentScreen());
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    protected void init() {
        super.init();
        window.init();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        super.close();
        EventManager.unregister(searchBar);
    }

    @Override
    public void renderContent(Window window, DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        searchBar.render(drawContext, x + windowWidth / 2 - 66, y, mouseX, mouseY, textRenderer);

        if (setting != null) {
            windowHeight = setting.handleRenderingEntries(drawContext, x + 1, y + 30, mouseX, mouseY, window);
            window.setWindowHeight(windowHeight + 60);
        }
    }
}
