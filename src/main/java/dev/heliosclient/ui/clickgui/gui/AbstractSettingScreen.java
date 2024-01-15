package dev.heliosclient.ui.clickgui.gui;


import com.google.common.util.concurrent.AtomicDouble;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.interfaces.IWindowContentRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class AbstractSettingScreen extends Screen implements IWindowContentRenderer {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    protected Module_ module;
    protected final Window window;
    protected Setting setting;
    protected HudElement hudElement;
    protected int x, y, windowWidth, windowHeight;
    public volatile AtomicDouble delay = new AtomicDouble(0.0D);

    public AbstractSettingScreen(Text title, Module_ module, int windowHeight, int windowWidth) {
        super(title);
        this.module = module;
        this.window = new Window(windowHeight, windowWidth, true, this);
    }
    public AbstractSettingScreen(Text title, Setting setting, int windowHeight, int windowWidth) {
        super(title);
        this.setting = setting;
        this.window = new Window(windowHeight, windowWidth, true, this);
    }
    public AbstractSettingScreen(Text title, HudElement hudElement, int windowHeight, int windowWidth) {
        super(title);
        this.hudElement = hudElement;
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
        if(module != null) {
            window.render(drawContext, mouseX, mouseY, module.name, module.description, textRenderer);
        }else if(setting != null){
            window.render(drawContext, mouseX, mouseY, setting.name, setting.description, textRenderer);
        } else if(hudElement != null){
            window.render(drawContext, mouseX, mouseY, hudElement.name, hudElement.description, textRenderer);
        }

        x = window.getX();
        y = window.getY();

        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(module != null) {
            for (SettingGroup settingGroup : module.settingGroups) {
                settingGroup.mouseClickedBuilder(mouseX, mouseY);
                if (!settingGroup.shouldRender()) continue;
                settingGroup.mouseClicked(mouseX, mouseY, button);
            }
        } else if (setting != null) {
            setting.mouseClicked(mouseX, mouseY, button);
        }
        else if(hudElement != null){
            for (SettingGroup settingGroup : hudElement.settingGroups) {
                settingGroup.mouseClickedBuilder(mouseX, mouseY);
                if (!settingGroup.shouldRender()) continue;
                settingGroup.mouseClicked(mouseX, mouseY, button);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(module != null) {
            for (SettingGroup settingGroup : module.settingGroups) {
                if (!settingGroup.shouldRender()) continue;
                settingGroup.mouseReleased(mouseX, mouseY, button);
            }
        } else if (setting != null) {
            setting.mouseReleased(mouseX, mouseY, button);
        }
        else if(hudElement != null){
            for (SettingGroup settingGroup : hudElement.settingGroups) {
                if (!settingGroup.shouldRender()) continue;
                settingGroup.mouseReleased(mouseX, mouseY, button);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(module != null) {
            for (SettingGroup settingGroup : module.settingGroups) {
                if (!settingGroup.shouldRender()) continue;
                settingGroup.keyPressed(keyCode, scanCode, modifiers);
            }
        } else if (setting != null) {
            setting.keyPressed(keyCode, scanCode, modifiers);
        }
        else if(hudElement != null){
            for (SettingGroup settingGroup : hudElement.settingGroups) {
                if (!settingGroup.shouldRender()) continue;
                settingGroup.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if(module != null) {
            for (SettingGroup settingGroup : module.settingGroups) {
                if (!settingGroup.shouldRender()) continue;
                settingGroup.charTyped(chr, modifiers);
            }
        } else if (setting != null) {
            setting.charTyped(chr, modifiers);
        }
        else if(hudElement != null){
            for (SettingGroup settingGroup : hudElement.settingGroups) {
                if (!settingGroup.shouldRender()) continue;
                settingGroup.charTyped(chr, modifiers);
            }
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
