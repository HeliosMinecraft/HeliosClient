package dev.heliosclient.ui.clickgui;

import dev.heliosclient.module.Module_;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;


public class ClientSettingsScreen extends Screen {
    private Module_ module;
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    public NavBar navBar = new NavBar();

    int x, y, windowWidth = 224, windowHeight;
    static int offsetY = 0;

    public ClientSettingsScreen(Module_ module) {
        super(Text.literal("Client Settings"));
        this.module = module;
        offsetY = 0;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);

        windowHeight = 52;
        for (Setting setting: module.settings) {
            if (!setting.shouldRender()) continue;
            setting.quickSettings=false;
            windowHeight += setting.height+1;
        }

        int screenHeight = drawContext.getScaledWindowHeight();

        if (drawContext.getScaledWindowHeight() > windowHeight) {
            offsetY = 0;
            y = drawContext.getScaledWindowHeight()/2-(windowHeight)/2;
        } else {
            offsetY = Math.max(Math.min(offsetY, 12), screenHeight-windowHeight);
            y = offsetY;
        }

        x = Math.max(drawContext.getScaledWindowWidth()/2-windowWidth/2, 0);

        Renderer2D.drawRoundedRectangle(drawContext,x, y, windowWidth, windowHeight,5, 0xFF222222);
        Renderer2D.drawRoundedRectangle(drawContext,x, y, windowWidth, 18,5, 0xFF1B1B1B);
        Renderer2D.drawRectangle(drawContext,x, y+16, windowWidth, 2, ColorManager.INSTANCE.clickGuiSecondary());
        drawContext.drawText(textRenderer, module.name, drawContext.getScaledWindowWidth()/2-textRenderer.getWidth(module.name)/2, y+4, ColorManager.INSTANCE.clickGuiPaneText(), false);
        drawContext.drawText(textRenderer, "§o" + module.description, drawContext.getScaledWindowWidth()/2-textRenderer.getWidth("§o"+module.description)/2, y+26, ColorManager.INSTANCE.defaultTextColor(), false);
        int yOffset = y+44;
        for (Setting setting : module.settings) {
            if (!setting.shouldRender()) continue;
            setting.render(drawContext,x+16,yOffset,mouseX,mouseY,textRenderer);
            yOffset += setting.height+1;
        }
        navBar.render(drawContext, textRenderer, mouseX, mouseY);
        Tooltip.tooltip.render(drawContext,textRenderer,mouseX,mouseY);
    }

    @Override
    public boolean shouldPause() {
        return ClickGUI.pause;
    }

    @Override
    public boolean mouseClicked(double mouseX,double mouseY,int button) {
        for (Setting setting : module.settings) {
            setting.mouseClicked(mouseX,mouseY,button);
        }
        navBar.mouseClicked((int) mouseX, (int) mouseY, button);
        return super.mouseClicked(mouseX,mouseY,button);
    }

    @Override
    public boolean mouseReleased(double mouseX,double mouseY,int button) {
        for (Setting setting : module.settings) {
            setting.mouseReleased(mouseX,mouseY,button);
        }
        return super.mouseReleased(mouseX,mouseY,button);
    }

    @Override
    public boolean keyPressed(int keyCode,int scanCode,int modifiers) {
        for (Setting setting : module.settings) {
            setting.keyPressed(keyCode,scanCode,modifiers);
        }
        return super.keyPressed(keyCode,scanCode,modifiers);
    }

    public static void onScroll(double horizontal,double vertical) {
        offsetY += vertical*(Easing.ease(EasingType.QUADRATIC_IN, (float) ClickGUI.ScrollSpeed.value));
    }
}
