package dev.heliosclient.ui.clickgui.settings;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.GradientManager;
import dev.heliosclient.module.settings.GradientSetting;
import dev.heliosclient.module.settings.RGBASetting;
import dev.heliosclient.module.settings.lists.ListSetting;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.ui.clickgui.gui.Window;
import dev.heliosclient.ui.clickgui.gui.tables.TableEntry;
import dev.heliosclient.util.interfaces.IWindowContentRenderer;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class GradientSettingScreen extends Screen implements IWindowContentRenderer {
    private final Window window;
    private final GradientSetting setting;
    int windowWidth = 192, windowHeight = 140;

    public GradientSettingScreen(GradientSetting setting) {
        super(Text.literal(setting.name == null ? "NULL" : setting.name));
        this.setting = setting;
        window = new Window(windowHeight, windowWidth, true, this);
        this.setting.createTable(windowWidth);
        adjustTable();
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) {
            super.renderBackgroundTexture(drawContext);
        }

        if (textRenderer.getWidth(setting.name) > windowWidth) {
            windowWidth = textRenderer.getWidth(setting.name) + 30;
            this.setting.createTable(windowWidth);
            adjustTable();
        }

        window.setWindowHeight(windowHeight);
        window.setWindowWidth(windowWidth);
        window.render(drawContext, mouseX, mouseY, setting.name, setting.description, textRenderer);


        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        window.setBackButtonTask(() -> {
            if (setting.getParentScreen() != null) {
                HeliosClient.MC.setScreen(setting.getParentScreen());
            }
        });

        for (List<TableEntry> row : this.setting.gradientTable.table) {
            for (TableEntry entry : row) {
                if (entry instanceof GradientSetting.GradientEntry gE) {
                    boolean isMouseOver = ListSetting.isMouseOver(mouseX,mouseY,(float) gE.x,(float) gE.y, (float)gE.width - 3, 18);

                    if(isMouseOver){
                        this.setting.setValue(gE.gradient);
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        window.mouseScrolled(mouseX, mouseY, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            setting.setHeight(25);
            if (setting.getParentScreen() != null) {
                HeliosClient.MC.setScreen(setting.getParentScreen());
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void adjustTable(){
        this.windowHeight = 30 + (int) this.setting.gradientTable.adjustTableLayout(window.getX() + 8, window.getContentY(),windowWidth - 15,false) ;
    }

    @Override
    protected void init() {
        super.init();
        window.init();
        this.setting.createTable(windowWidth);
        adjustTable();
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        window.init();
        adjustTable();
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
    public void renderContent(Window window, DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        if (setting != null) {
            adjustTable();
            setting.renderAllGradients(drawContext,mouseX,mouseY);
        }
    }
}
