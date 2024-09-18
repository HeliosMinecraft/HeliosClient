package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.heliosclient.FontChangeEvent;
import dev.heliosclient.event.events.input.MouseClickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.render.GUI;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.gui.HudBox;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CategoryPane implements Listener {
    public static int width = 85;
    public static int MAX_HEIGHT = 150;

    //Collection of all CategoryPane's hudBoxes
    public static List<HudBox> hudBoxes = new ArrayList<>();
    public final char icon;
    private final Screen parentScreen;
    private final HudBox hudBox;
    public int maxWidth = 0;
    public Category category;
    public int x;
    public int y;
    public int height;
    public boolean collapsed;
    public Screen screen;
    protected double scale = 0.0f;
    int startX, startY;
    boolean dragging = false;
    List<ModuleButton> moduleButtons;
    int categoryNameHeight = 2 , categoryNameWidth = 2;
    private int scrollOffset = 0;


    public CategoryPane(Category category, int initialX, int initialY, boolean collapsed, Screen parentScreen) {
        this.category = category;
        this.x = initialX;
        this.y = initialY;
        this.collapsed = collapsed;
        this.parentScreen = parentScreen;
        moduleButtons = new CopyOnWriteArrayList<ModuleButton>();
        this.maxWidth = 0;
        this.height = 4;
        for (Module_ m : ModuleManager.getModulesByCategory(category)) {
            ModuleButton mb = new ModuleButton(m, parentScreen);
            moduleButtons.add(mb);
            maxWidth = Math.max(maxWidth, mb.width - 2);
            height += ModuleButton.height + 3;
        }
        if (maxWidth < getWidth() - 2) {
            maxWidth = getWidth() - 2;
        }

        checkMaxHeightValue();

        icon = category.icon;

        hudBox = new HudBox(x, y, width, height);
        hudBoxes.add(hudBox);

        EventManager.register(this);
    }


    public static int getWidth() {
        return width;
    }

    public void addModule(List<Module_> moduleS) {

        for (Module_ module : moduleS) {
            boolean exists = false;
            for (ModuleButton button : moduleButtons) {
                if (button.module == module) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                ModuleButton moduleButton = new ModuleButton(module, parentScreen);
                moduleButtons.add(moduleButton);
            }
        }

        calcHeightAndWidth();
    }

    public void calcHeightAndWidth() {
        height = 4;
        for (ModuleButton button : moduleButtons) {
            maxWidth = Math.max(maxWidth, button.width - 2);
            height += ModuleButton.height + 3;
        }

        if (maxWidth < getWidth() - 2) {
            maxWidth = getWidth() - 2;
        }
    }

    public void checkMaxHeightValue() {
        if (ClickGUI.ScrollTypes.values()[HeliosClient.CLICKGUI.ScrollType.value] == ClickGUI.ScrollTypes.OLD) {
            MAX_HEIGHT = height;
        } else {
            MAX_HEIGHT = (int) Math.round(HeliosClient.CLICKGUI.CategoryHeight.value);
            if (MAX_HEIGHT > height) {
                MAX_HEIGHT = height;
            }
        }
    }

    public void update(float delta) {
        // Update the scale
        scale += delta;
        if (scale > 1.0f) {
            scale = 1.0f;
        }
    }


    public void removeModule(Module_ module) {
        moduleButtons.removeIf(button -> button.module == module);
    }

    public void keepOnlyModule(Module_ module) {
        moduleButtons.removeIf(button -> button.module != module);
        height = 22;
    }

    public void removeModules() {
        if(!moduleButtons.isEmpty())
            moduleButtons.clear();

        height = 4;
        maxWidth = 0;
    }

    @SubscribeEvent
    public void onFontChange(FontChangeEvent e) {
        categoryNameHeight = Math.round(Renderer2D.getFxStringHeight(category.name));
        categoryNameWidth = Math.round(Renderer2D.getFxStringWidth(category.name));
        maxWidth = 0;
        calcHeightAndWidth();
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta, TextRenderer textRenderer) {
        update(delta * (float) HeliosClient.CLICKGUI.animationSpeed.value);
        GUI gui = ModuleManager.get(GUI.class);

        this.screen = HeliosClient.MC.currentScreen;

        checkMaxHeightValue();

        if (dragging) {
            x = mouseX - startX;
            y = mouseY - startY;
        }

        Renderer2D.scaleAndPosition(drawContext.getMatrices(), (x + (width + 5) / 2.0f), (float) (y + categoryNameHeight + 6), (float) scale);

        if (!collapsed && height >= 10) {
            if (gui.categoryBorder.value) {
                drawOutlineGradientBox(drawContext.getMatrices().peek().getPositionMatrix(), x, y, width);
            }
            drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x - 1, y + categoryNameHeight + 6, false, false, true, true, width + 2f, hudBox.getHeight(),  HeliosClient.CLICKGUI.guiRoundness.getInt() + 1,ColorManager.INSTANCE.clickGuiPrimary);
            // Apply the scale factor to the scissor box dimensions
            Renderer2D.enableScissor(x - 20, (int) ((y + categoryNameHeight + 6)), width + 40, (int) ((hudBox.getHeight() - 1)));
        }

        if (collapsed) {
            scale = 0.0f;
        } else {
            int buttonYOffset = y + 10 + categoryNameHeight - scrollOffset;
            for (ModuleButton m : moduleButtons) {
                boolean shouldModuleRender = buttonYOffset > y + 5;

                m.render(drawContext, mouseX, mouseY, x, buttonYOffset, maxWidth, shouldModuleRender);

                int settingsHeight = m.renderSettings(drawContext, x, buttonYOffset, mouseX, mouseY, textRenderer);
                buttonYOffset += settingsHeight;

                MAX_HEIGHT = settingsHeight + MAX_HEIGHT;

                buttonYOffset += ModuleButton.height + 3;
            }
            Renderer2D.disableScissor();
        }
        Renderer2D.stopScaling(drawContext.getMatrices());

        drawRectangleWithShadow(drawContext.getMatrices(), x - 2, y, true, true, true, true, width + 4.5f, categoryNameHeight + 8,  HeliosClient.CLICKGUI.guiRoundness.getInt() + 1, HeliosClient.CLICKGUI.guiRoundness.getInt(), ModuleManager.get(GUI.class).categoryPaneColors.getWithAlpha(255).getRGB());

        Renderer2D.drawFixedString(drawContext.getMatrices(), category.name, x + (float) (CategoryPane.getWidth() - 4) / 2 - categoryNameWidth / 2.0f, (float) (y + 4), ColorManager.INSTANCE.clickGuiPaneText());


        hudBox.set(x, y, width, MAX_HEIGHT);

        if (gui.syncCategoryIconColor.value) {
            FontRenderers.iconRenderer.drawString(drawContext.getMatrices(), String.valueOf(icon), x + 1, (float) (y + 3), ColorManager.INSTANCE.getPrimaryGradientStart().getRGB());
        }else {
            FontRenderers.iconRenderer.drawString(drawContext.getMatrices(), String.valueOf(icon), x + 1, (float) (y + 3), -1);
        }

        if(gui.displayModuleCount.value){
            String modulesAmount = "["+moduleButtons.size()+"]";
            Renderer2D.drawFixedString(drawContext.getMatrices(), modulesAmount, x + (float) (CategoryPane.getWidth() - 4 - Renderer2D.getFxStringWidth(modulesAmount)), (float) (y + 4), -1);
        }
    }

    public boolean hovered(double mouseX, double mouseY) {
        return mouseX >= x - 2 && mouseX <= x + width + 4.5f && mouseY >= y && mouseY <= y + categoryNameHeight + 8;
    }

    public boolean hoveredOverModules(double mouseX, double mouseY) {
        return mouseX > x + 2 && mouseX < x + (width - 2) && mouseY > y + categoryNameHeight + 14 && mouseY < y + height;
    }

    @SubscribeEvent
    public void mouseClicked(MouseClickEvent event) {

        //Do not accept any clicks if the mouse is over the search bar
        if (ClickGUIScreen.INSTANCE.searchBar.isMouseOverInputBox(event.getMouseX(), event.getMouseY())) return;


        if (screen != null && event.getScreen() == screen) {
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            int button = event.getButton();

            if (hovered(mouseX, mouseY) && button == 1) toggleCollapsed();
            else if (hovered(mouseX, mouseY) && button == 0) {
                startX = (int) (mouseX - x);
                startY = (int) (mouseY - y);
                dragging = true;
            }
            if (button == 2) {
                startX = (int) mouseX - x;
                startY = (int) mouseY - y;
                dragging = true;
            }

            if (!hovered(mouseX, mouseY)) {
                for (ModuleButton moduleButton : moduleButtons) {
                    moduleButton.collapsed = collapsed;
                    moduleButton.mouseClicked(mouseX, mouseY, button, event.getScreen());
                }
            }
        }
    }

    private void drawOutlineGradientBox(Matrix4f matrix4f, float x, float y, float width) {
        if (HeliosClient.CLICKGUI.getTheme() == ClickGUI.Theme.Rounded) {
            Renderer2D.drawOutlineGradientRoundedBox(matrix4f, x - 1, y + categoryNameHeight, width + 2f, hudBox.getHeight() + 6,  HeliosClient.CLICKGUI.guiRoundness.getInt() + 1, 1f, ColorManager.INSTANCE.getPrimaryGradientStart(), ColorManager.INSTANCE.getPrimaryGradientStart(), ColorManager.INSTANCE.getPrimaryGradientEnd(), ColorManager.INSTANCE.getPrimaryGradientEnd());
        } else {
            Renderer2D.drawOutlineGradientBox(matrix4f, x - 1, y + categoryNameHeight, width + 2f, hudBox.getHeight() + 6, 1f, ColorManager.INSTANCE.getPrimaryGradientStart(), ColorManager.INSTANCE.getPrimaryGradientStart(), ColorManager.INSTANCE.getPrimaryGradientEnd(), ColorManager.INSTANCE.getPrimaryGradientEnd());
        }
    }

    private void drawRectangle(Matrix4f matrix4f, float x, float y, boolean TL, boolean TR, boolean BL, boolean BR, float width, float height, float radius, int color) {
        if (HeliosClient.CLICKGUI.getTheme() == ClickGUI.Theme.Rounded) {
            Renderer2D.drawRoundedRectangle(matrix4f, x, y, TL, TR, BL, BR, width, height, radius, color);
        } else {
            Renderer2D.drawRectangle(matrix4f, x, y, width, height, color);
        }
    }

    private void drawRectangleWithShadow(MatrixStack stack, float x, float y, boolean TL, boolean TR, boolean BL, boolean BR, float width, float height, float radius, int blurRadius, int color) {
        if (HeliosClient.CLICKGUI.getTheme() == ClickGUI.Theme.Rounded) {
            Renderer2D.drawRoundedRectangleWithShadow(stack, x, y, width, height, radius, blurRadius, color, TL, TR, BL, BR);
        } else {
            Renderer2D.drawRectangleWithShadow(stack, x, y, width, height, color, blurRadius);
        }
    }

    public void toggleCollapsed() {
        collapsed = !collapsed;
        if (collapsed) {
            scale = 0.0f;
        }
    }

    public void mouseScrolled(int mouseX, int mouseY, double amount) {
        if (hoveredOverModules(mouseX, mouseY) && ClickGUI.ScrollTypes.values()[HeliosClient.CLICKGUI.ScrollType.value] == ClickGUI.ScrollTypes.NEW) {
            // Scroll this pane by changing the scroll offset
            scrollOffset += (int) (amount * HeliosClient.CLICKGUI.ScrollSpeed.value);
            scrollOffset = Math.max(scrollOffset,0);
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        for (ModuleButton moduleButton : moduleButtons) {
            if (moduleButton.settingsOpen) {
                for (Setting<?> setting : moduleButton.module.quickSettings) {
                    if (!setting.shouldRender()) continue;
                    setting.mouseReleased(mouseX, mouseY, button);
                }
            }
        }
        dragging = false;
    }

    public void charTyped(char chr, int modifiers) {
        for (ModuleButton moduleButton : moduleButtons) {
            if (moduleButton.settingsOpen) {
                for (Setting<?> setting : moduleButton.module.quickSettings) {
                    if (!setting.shouldRender()) continue;
                    setting.charTyped(chr, modifiers);
                }
            }
        }
    }
}
