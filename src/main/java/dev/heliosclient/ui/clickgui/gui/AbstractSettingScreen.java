package dev.heliosclient.ui.clickgui.gui;


import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.util.interfaces.IWindowContentRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class AbstractSettingScreen extends Screen implements IWindowContentRenderer {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    protected final Module_ module;
    protected final Window window;
    protected int x, y, windowWidth, windowHeight;
    public float delay = 0;

    public AbstractSettingScreen(Text title, Module_ module, int windowHeight, int windowWidth) {
        super(title);
        this.module = module;
        this.window = new Window(windowHeight, windowWidth, true, this);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        window.mouseScrolled(mouseX, mouseY, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void init() {
        super.init();
        window.init();
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        window.render(drawContext, mouseX, mouseY, module.name, module.description, textRenderer);
        x = window.getX();
        y = window.getY();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        window.mouseClicked(mouseX, mouseY);
        for (SettingGroup settingGroup : module.settingGroups) {
            settingGroup.mouseClickedBuilder(mouseX, mouseY);
            if (!settingGroup.shouldRender()) continue;
            settingGroup.mouseClicked(mouseX, mouseY, button);
        }
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
    public boolean shouldPause() {
        return ClickGUI.pause;
    }

    public abstract void updateSetting();

    @Override
    public abstract void renderContent(Window window, DrawContext drawContext, int x, int y, int mouseX, int mouseY);
}
