package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.CategoryManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.system.Config;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClickGUIScreen extends Screen {
    public static ClickGUIScreen INSTANCE = new ClickGUIScreen();
    static int scrollX = 0;
    static int scrollY = 0;
    public ArrayList<CategoryPane> categoryPanes;
    public SearchBar searchBar;

    public ClickGUIScreen() {
        super(Text.literal("ClickGUI"));
        reset();
    }

    public void reset() {
        scrollX = 0;
        scrollY = 0;
        categoryPanes = new ArrayList<>();

        Object panesObject = HeliosClient.CONFIG.modulesManager.getConfigMaps().get(Config.MODULES).get("panes");
        if (panesObject instanceof Map<?, ?> panePos) {
            CategoryManager.getCategories().forEach((s, category) -> {
                Object categoryObject = panePos.get(category.name);
                if (categoryObject instanceof Map<?, ?> map) {
                    int xOffset = MathUtils.d2iSafe(map.get("x"));
                    int yOffset = MathUtils.d2iSafe(map.get("y"));
                    boolean collapsed = (boolean) map.get("collapsed");
                    categoryPanes.add(new CategoryPane(category, xOffset, yOffset, collapsed, this));
                }
            });
        }
        searchBar = new SearchBar();
        EventManager.register(searchBar);
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        //Rerun for any missed font changes.
        if(categoryPanes != null && !categoryPanes.isEmpty()){
            for(CategoryPane pane: categoryPanes){
                pane.onFontChange(null);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (CategoryPane category : categoryPanes) {
            category.mouseScrolled((int) mouseX, (int) mouseY, verticalAmount);
        }

        if (ClickGUI.ScrollTypes.values()[HeliosClient.CLICKGUI.ScrollType.value] == ClickGUI.ScrollTypes.OLD) {
            // Old mode: scroll the whole screen
            scrollY += (int) verticalAmount;
        }
        scrollX += (int) horizontalAmount;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) {
            super.renderBackgroundTexture(drawContext);
        }

        if (HeliosClient.CLICKGUI.ScreenHelp.value) {
            float fontHeight = Renderer2D.getCustomStringHeight(FontRenderers.Super_Small_fxfontRenderer);
            FontRenderers.Super_Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Left Click - Toggle Module", 2, drawContext.getScaledWindowHeight() - (3 * fontHeight) - 3 * 2, -1);
            FontRenderers.Super_Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Middle Click - Open QuickSettings", 2, drawContext.getScaledWindowHeight() - (2 * fontHeight) - 2 * 2, -1);
            FontRenderers.Super_Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Right Click - Open Settings", 2, drawContext.getScaledWindowHeight() - (fontHeight) - 2, -1);
        }

        for (CategoryPane category : categoryPanes) {
            category.y += scrollY * 10;
            category.x += scrollX * 10;
            category.render(drawContext, mouseX, mouseY, delta, textRenderer);

            //Search
            if (category.category == Categories.SEARCH && !category.collapsed) {
                if (searchBar.getValue().isEmpty()) {
                    category.removeModules();
                } else {
                    List<Module_> modulesToAdd = ModuleManager.getModuleByNameSearch(searchBar.getValue(), 10);

                    category.addModule(modulesToAdd);
                    if (modulesToAdd.size() == 1) {
                        category.keepOnlyModule(modulesToAdd.get(0));
                    }
                }
            }
        }

        searchBar.render(drawContext, drawContext.getScaledWindowWidth() / 2 - searchBar.width / 2 + 7, drawContext.getScaledWindowHeight() - searchBar.height - 11, mouseX, mouseY, textRenderer);
        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);
        NavBar.navBar.render(drawContext, textRenderer, mouseX, mouseY);
        scrollY = 0;
        scrollX = 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        NavBar.navBar.mouseClicked((int) mouseX, (int) mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_S && Screen.hasControlDown() && !searchBar.isFocused()) {
            searchBar.setFocused(true);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryPane category : categoryPanes) {
            category.mouseReleased((int) mouseX, (int) mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (CategoryPane category : categoryPanes) {
            category.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return ClickGUI.pause;
    }

    public void onLoad() {
        scrollY = 0;
        scrollX = 0;
    }
}

