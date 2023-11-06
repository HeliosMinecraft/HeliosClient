package dev.heliosclient.ui.clickgui;

import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import dev.heliosclient.ui.clickgui.ui.Window;
import dev.heliosclient.util.interfaces.IWindowContentRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


public class ClientSettingsScreen extends Screen implements IWindowContentRenderer {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    static int offsetY = 0;
    public NavBar navBar = new NavBar();

    int x, y, windowWidth = 224, windowHeight;
    private final Module_ module;
    private final Window window;

    public ClientSettingsScreen(Module_ module) {
        super(Text.literal("Client Settings"));
        this.module = module;
        offsetY = 0;
        window = new Window(windowHeight, windowWidth, true, this);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        window.mouseScrolled(mouseX, mouseY, amount);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    protected void init() {
        super.init();
        window.init();
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);

        windowHeight = 45;
        for (SettingGroup settingGroup : module.settingGroups) {
            windowHeight += Math.round(settingGroup.getGroupNameHeight() + 3);
            if (!settingGroup.shouldRender()) continue;
            for (Setting setting : settingGroup.getSettings()) {
                if (!setting.shouldRender()) continue;
                setting.quickSettings = false;
                windowHeight += setting.height + 1;
            }
            windowHeight += Math.round(settingGroup.getGroupNameHeight());
        }

        window.setWindowHeight(windowHeight);
        window.render(drawContext, mouseX, mouseY, module.name, module.description, textRenderer);


        navBar.render(drawContext, textRenderer, mouseX, mouseY);
        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);
        FontManager.fontSize = ClickGUI.FontSize.value.intValue();
    }

    @Override
    public boolean shouldPause() {
        return ClickGUI.pause;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        window.mouseClicked(mouseX, mouseY);
        for (SettingGroup settingGroup : module.settingGroups) {
            settingGroup.mouseClickedBuilder(mouseX, mouseY);
            if (!settingGroup.shouldRender()) continue;
            settingGroup.mouseClicked(mouseX, mouseY, button);
        }
        navBar.mouseClicked((int) mouseX, (int) mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (SettingGroup settingGroup : module.settingGroups) {
            if (!settingGroup.shouldRender()) continue;
            settingGroup.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (SettingGroup settingGroup : module.settingGroups) {
            if (!settingGroup.shouldRender()) continue;
            settingGroup.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (SettingGroup settingGroup : module.settingGroups) {
            if (!settingGroup.shouldRender()) continue;
            settingGroup.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void renderContent(Window window, DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        int yOffset = y;
        for (SettingGroup settingGroup : module.settingGroups) {
            yOffset += Math.round(settingGroup.getGroupNameHeight() + 3);
            settingGroup.renderBuilder(drawContext, x + 16, yOffset - 3, windowWidth - 32);
            if (!settingGroup.shouldRender()) continue;
            for (Setting setting : settingGroup.getSettings()) {
                if (!setting.shouldRender()) continue;
                if (setting instanceof RGBASetting rgbaSetting) {
                    rgbaSetting.setParentScreen(this);
                }
                setting.render(drawContext, x + 16, yOffset + 3, mouseX, mouseY, textRenderer);
                yOffset += setting.height + 1;
            }
            yOffset += Math.round(settingGroup.getGroupNameHeight() + 1);
        }
    }
}
