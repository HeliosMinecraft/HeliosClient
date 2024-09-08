package dev.heliosclient.ui.clickgui;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.CategoryManager;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.render.GUI;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.gui.PolygonMeshPatternRenderer;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClickGUIScreen extends Screen {
    public static ClickGUIScreen INSTANCE = new ClickGUIScreen();
    static int scrollX = 0;
    static int scrollY = 0;
    public final ArrayList<CategoryPane> categoryPanes;
    public SearchBar searchBar;
    private float scale = 0.0f;
    private long timeOnOpen = 0;
    private boolean shouldClose = false;

    public ClickGUIScreen() {
        super(Text.literal("ClickGUI"));

        categoryPanes = new ArrayList<>();

        reset();
    }

    public void reset() {
        scrollX = 0;
        scrollY = 0;

        categoryPanes.forEach(EventManager::unregister);

        categoryPanes.clear();

        Object panesObject = HeliosClient.CONFIG.moduleConfigManager.getCurrentConfig().getReadData().get("panes");
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
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);

        PolygonMeshPatternRenderer.INSTANCE.create();
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        shouldClose = false;
        timeOnOpen = System.currentTimeMillis();

        //Rerun for any missed font changes.
        if (categoryPanes != null && !categoryPanes.isEmpty()) {
            for (CategoryPane pane : categoryPanes) {
                pane.onFontChange(null);
            }
        }
    }

    private void updateScale(float speed, boolean close) {
        if (ModuleManager.get(GUI.class).bounceAnimation.value) {
            long currentTime = System.currentTimeMillis();
            if (close) {
                this.scale -= speed * Easing.ease(EasingType.BOUNCE_OUT, MathHelper.clamp((currentTime - timeOnOpen) / 2000f, 0, 1));
                if (this.scale <= 0.0) {
                    super.close();
                    shouldClose = false;
                }
            } else {
                this.scale += speed * Easing.ease(EasingType.QUADRATIC_IN, MathHelper.clamp((currentTime - timeOnOpen) / 1000f, 0, 1));
            }
            this.scale = MathHelper.clamp(this.scale, 0.0f, 1f);
        } else {
            this.scale = 1.0f;
            if (shouldClose) {
                super.close();
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
        GUI gui = ModuleManager.get(GUI.class);
        Renderer2D.setDrawContext(drawContext);

        updateScale((float) HeliosClient.CLICKGUI.animationSpeed.value, shouldClose);
        if (this.client.world == null) {
            super.renderBackgroundTexture(drawContext);
        } else if (gui.background.value) {
            Renderer2D.drawGradient(drawContext.getMatrices().peek().getPositionMatrix(), 0, 0, width, height, ColorUtils.changeAlphaGetInt(ColorManager.INSTANCE.getPrimaryGradientStart().getRGB(), 50), ColorUtils.changeAlphaGetInt(ColorManager.INSTANCE.getPrimaryGradientEnd().getRGB(), 50), Renderer2D.Direction.LEFT_RIGHT);
        }

        if (gui.coolVisuals.value) {
            PolygonMeshPatternRenderer.INSTANCE.render(drawContext.getMatrices(), mouseX, mouseY);
        }

        if (HeliosClient.CLICKGUI.ScreenHelp.value) {
            Renderer2D.drawBottomText(drawContext.getMatrices(), 2, FontRenderers.Super_Small_fxfontRenderer,
                    "Right Click - Open Settings",
                    "Middle Click - Open QuickSettings",
                    "Left Click - Toggle Module",
                    "Ctrl + S - Search instantly"
            );
        }
        Renderer2D.scaleAndPosition(drawContext.getMatrices(), 0, 0, width, height, scale);

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

        Renderer2D.stopScaling(drawContext.getMatrices());

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
    public void close() {
        shouldClose = true;
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

