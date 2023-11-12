package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.CategoryManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Map;

public class ClickGUIScreen extends Screen {
    public static ClickGUIScreen INSTANCE = new ClickGUIScreen();
    static int scrollX = 0;
    static int scrollY = 0;
    public ArrayList<CategoryPane> categoryPanes;
    public InputBox searchBox;

    public ClickGUIScreen() {
        super(Text.literal("ClickGUI"));
        scrollX = 0;
        scrollY = 0;
        categoryPanes = new ArrayList<CategoryPane>();
        Map<String, Object> panePos = ((Map<String, Object>) HeliosClient.CONFIG.config.get("panes"));

        CategoryManager.getCategories().forEach((s, category) -> {
                    int xOffset = MathUtils.d2iSafe((((Map<String, Object>) panePos.get(category.name)).get("x")));
                    int yOffset = MathUtils.d2iSafe(((Map<String, Object>) panePos.get(category.name)).get("y"));
                    boolean collapsed = (boolean) ((Map<String, Object>) panePos.get(category.name)).get("collapsed");
                    categoryPanes.add(new CategoryPane(category, xOffset, yOffset, collapsed, this));
                }
        );

        searchBox = new InputBox(CategoryPane.getWidth() - 4, 12, "", 20, InputBox.InputMode.DIGITS_AND_CHARACTERS_AND_WHITESPACE);
    }

    public static void onScroll(double horizontal, double vertical) {
        if (ClickGUI.ScrollTypes.values()[ClickGUI.ScrollType.value] == ClickGUI.ScrollTypes.OLD) {
            // Old mode: scroll the whole screen
            scrollY += vertical;
        }
        scrollX += horizontal;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);
        for (CategoryPane category : categoryPanes) {
            category.y += scrollY * 10;
            category.x += scrollX * 10;
            category.render(drawContext, mouseX, mouseY, delta, textRenderer);
            if (category.category == Categories.SEARCH && !category.collapsed) {

                Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), category.x, category.y + 17, CategoryPane.getWidth(), 18, 0xFF1B1B1B);

                searchBox.render(drawContext, category.x, category.y + 19, mouseX, mouseY, textRenderer);

                category.addModule(ModuleManager.INSTANCE.getModuleByNameSearch(searchBox.getValue()));

                if (ModuleManager.INSTANCE.getModuleByNameSearch(searchBox.getValue()).size() == 1) {
                    category.keepOnlyModule(ModuleManager.INSTANCE.getModuleByNameSearch(searchBox.getValue()).get(0));
                }

                if (searchBox.getValue().isEmpty()) {
                    category.removeModules();
                }

            }
        }
        Tooltip.tooltip.render(drawContext, textRenderer, mouseX, mouseY);
        NavBar.navBar.render(drawContext, textRenderer, mouseX, mouseY);
        scrollY = 0;
        scrollX = 0;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        for (CategoryPane category : categoryPanes) {
            category.mouseScrolled((int) mouseX, (int) mouseY, amount);
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryPane category : categoryPanes) {
            if (category.category == Categories.SEARCH && !category.collapsed) {
                searchBox.mouseClicked(mouseX, mouseY, button);
            }
            category.mouseClicked((int) mouseX, (int) mouseY, button);
        }
        NavBar.navBar.mouseClicked((int) mouseX, (int) mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
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

