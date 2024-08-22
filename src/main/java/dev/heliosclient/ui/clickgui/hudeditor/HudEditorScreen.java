package dev.heliosclient.ui.clickgui.hudeditor;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.input.KeyPressedEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.HudManager;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.gui.HudBox;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import dev.heliosclient.util.TimerUtils;
import dev.heliosclient.util.animation.Animation;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HudEditorScreen extends Screen implements Listener {
    public static HudEditorScreen INSTANCE = new HudEditorScreen();
    private final List<HudElement> selectedElements = new ArrayList<>();
    float thickness = 0.5f; // Thickness of the alignment lines
    float threshold = 1; // Threshold between two HudBoxes for the alignment lines to show.
    // Variables to track the drag state and initial position
    private boolean isDragging = false;
    private HudBox dragBox = null;
    private final Map<String, Object> copiedSettings = new HashMap<>();
    private final int defaultSnapSize = 120;
    private boolean isScrolling;
    Animation fadeAnimation = new Animation(EasingType.LINEAR_IN);
    private final Color LIGHT_YELLOW = new Color(223, 248, 89, 121);
    private TimerUtils scrollTimer = new TimerUtils();
    private static final long SCROLL_TIMEOUT = 2000; // 2 seconds

    private HudEditorScreen() {
        super(Text.of("Hud editor"));
        EventManager.register(this);
        dragBox = new HudBox(0, 0, 0, 0);
        selectedElements.clear();
        fadeAnimation.setFadeSpeed(0.01f);
        fadeAnimation.setAlpha(0);
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        selectedElements.clear();
        scrollTimer.startTimer();
    }

    @Override
    public void close() {
        super.close();
        selectedElements.clear();
        copiedSettings.clear();
        isDragging = false;
        dragBox = null;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) {
            super.renderBackgroundTexture(drawContext);
        }

        if (HeliosClient.CLICKGUI.ScreenHelp.value) {
            float fontHeight = Renderer2D.getCustomStringHeight(FontRenderers.Super_Small_fxfontRenderer);
            FontRenderers.Super_Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Shift + Scroll - To increase/decrease snap size", 2, drawContext.getScaledWindowHeight() - (6 * fontHeight) - 6 * 2, -1);
            FontRenderers.Super_Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Ctrl- C/V - To Copy/Paste HudElement properties", 2, drawContext.getScaledWindowHeight() - (5 * fontHeight) - 5 * 2, -1);
            FontRenderers.Super_Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Left Click - Select Element", 2, drawContext.getScaledWindowHeight() - (4 * fontHeight) - 4 * 2, -1);
            FontRenderers.Super_Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Right Click - Open Settings", 2, drawContext.getScaledWindowHeight() - (3 * fontHeight) - 3 * 2, -1);
            FontRenderers.Super_Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Delete / Backspace - Remove Element", 2, drawContext.getScaledWindowHeight() - (2 * fontHeight) - 2 * 2, -1);
            FontRenderers.Super_Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Hold Shift Key - Enable Snapping", 2, drawContext.getScaledWindowHeight() - fontHeight - 2, -1);
        }

        checkAlignmentAndDrawLines(drawContext);

        if (dragBox != null && !HudCategoryPane.INSTANCE.hoveredOverCategoryCompletely(mouseX, mouseY)) {
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(0, 0, -0.0D);
            drawContext.fill(Math.round(dragBox.getX()), Math.round(dragBox.getY()), Math.round(dragBox.getX() + dragBox.getWidth()), Math.round(dragBox.getY() + dragBox.getHeight()), new Color(255, 255, 255, 45).getRGB());
            drawContext.drawBorder(Math.round(dragBox.getX() - 1), Math.round(dragBox.getY() - 1), Math.round(dragBox.getWidth() + 1), Math.round(dragBox.getHeight() + 1), new Color(255, 255, 255, 155).getRGB());
            drawContext.getMatrices().pop();
        }

        //Fade out every SCROLL_TIMEOUT ms
        scrollTimer.every(SCROLL_TIMEOUT,()-> {
            if(fadeAnimation.getInterpolatedAlpha() > 0.7f) {
                fadeAnimation.startFading(false);
            }
        });

        String sizeChangeText = "Snap Size changed to " + HudElement.getSnapSize();
        float textWidth = Renderer2D.getFxStringWidth(sizeChangeText);
        float textHeight = Renderer2D.getFxStringHeight(sizeChangeText);
        float x = this.width / 2f - textWidth / 2f;
        float y = this.height - textHeight * 2f;
        AnimationUtils.drawFadingAndPoppingBoxBetter(Renderer2D.drawContext,fadeAnimation,x - 2,y - 2,textWidth + 4,textHeight + 4,LIGHT_YELLOW.getRGB(),true,3f);
        AnimationUtils.drawFadingAndPoppingText(Renderer2D.drawContext,fadeAnimation,sizeChangeText,x,y,-1,true);

        HudManager.INSTANCE.renderEditor(drawContext, textRenderer, mouseX, mouseY);
        HudCategoryPane.INSTANCE.render(drawContext, textRenderer, mouseX, mouseY, delta);
        NavBar.navBar.render(drawContext, textRenderer, mouseX, mouseY);
    }

    public void checkAlignmentAndDrawLines(DrawContext drawContext) {
        List<HudElement> hudElements = HudManager.INSTANCE.hudElements;
        for (int i = 0; i < hudElements.size(); i++) {
            HudElement element1 = hudElements.get(i);
            if (!element1.selected) {
                continue;
            }
            for (int j = 0; j < hudElements.size(); j++) {
                if (i == j) {
                    continue; // Skip comparison with itself
                }
                HudElement element2 = hudElements.get(j);
                checkAndDrawVerticalLine(drawContext, element1.hudBox.getLeft(), element2.hudBox.getLeft(), element2.hudBox.getX());

                checkAndDrawVerticalLine(drawContext, element1.hudBox.getRight(), element2.hudBox.getRight(), element2.hudBox.getX() + element2.hudBox.getWidth());

                checkAndDrawHorizontalLine(drawContext, element1.hudBox.getTop(), element2.hudBox.getTop(), element2.hudBox.getY() - 1);

                checkAndDrawHorizontalLine(drawContext, element1.hudBox.getBottom(), element2.hudBox.getBottom(), element2.hudBox.getY() + element2.hudBox.getHeight());
            }
        }
    }

    private void checkAndDrawVerticalLine(DrawContext drawContext, float side1, float side2, float x) {
        if (Math.abs(side1 - side2) < threshold) {
            Renderer2D.drawVerticalLine(drawContext.getMatrices().peek().getPositionMatrix(), x, 0, this.height, thickness, Color.WHITE.getRGB());
        }
    }

    private void checkAndDrawHorizontalLine(DrawContext drawContext, float side1, float side2, float y) {
        if (Math.abs(side1 - side2) < threshold) {
            Renderer2D.drawHorizontalLine(drawContext.getMatrices().peek().getPositionMatrix(), 0, this.width, y, thickness, Color.WHITE.getRGB());
        }
    }


    @Override
    public boolean shouldPause() {
        return ClickGUI.pause;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        NavBar.navBar.mouseClicked((int) mouseX, (int) mouseY, button);
        HudCategoryPane.INSTANCE.mouseClicked(mouseX, mouseY, button);

        if (button == 0) {
            if (!selectedElements.isEmpty() && !isDragging && dragBox == null && !HudCategoryPane.INSTANCE.hoveredOverCategoryCompletely(mouseX, mouseY)) {
                for (HudElement element : selectedElements) {
                    if (dragBox == null && !isDragging) {
                        element.mouseClicked(mouseX, mouseY, button);
                    }
                }
            }
        }
        selectedElements.clear();

        int lastHoveredIndex = -1;
        for (int i = HudManager.INSTANCE.hudElements.size() - 1; i >= 0; i--) {
            if (HudManager.INSTANCE.hudElements.get(i).hovered(mouseX, mouseY)) {
                HudManager.INSTANCE.hudElements.get(i).selected = true;
                if (button == 1) {
                    HudManager.INSTANCE.hudElements.get(i).selected = false;
                    HeliosClient.MC.setScreen(new HudEditorSettingScreen(HudManager.INSTANCE.hudElements.get(i)));
                }
                lastHoveredIndex = i;
                break;
            }
        }

        for (int i = 0; i < HudManager.INSTANCE.hudElements.size(); i++) {
            if (i != lastHoveredIndex) {
                HudManager.INSTANCE.hudElements.get(i).selected = false;
            }
            if (HudManager.INSTANCE.hudElements.get(i).selected) {
                HudManager.INSTANCE.hudElements.get(i).mouseClicked(mouseX, mouseY, button);
            }
        }

        if (button == 0) {
            if (lastHoveredIndex == -1 && selectedElements.isEmpty() && !HudCategoryPane.INSTANCE.hoveredOverCategoryCompletely(mouseX, mouseY) && dragBox == null) {
                isDragging = true;
                int dragStartX = (int) mouseX;
                int dragStartY = (int) mouseY;
                dragBox = new HudBox(dragStartX, dragStartY, 0, 0);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void selectElementsInRectangle(HudBox dragBox) {
        for (HudElement element : HudManager.INSTANCE.hudElements) {
            if (element.hudBox.intersects(dragBox)) {
                element.selected = true;
                selectedElements.add(element);
            } else {
                selectedElements.remove(element);
                element.selected = false;
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        HudCategoryPane.INSTANCE.mouseReleased(mouseX, mouseY, button);

        for (HudElement element : HudManager.INSTANCE.hudElements) {
            element.mouseReleased(mouseX, mouseY, button);
        }
        // Stop dragging when the user releases the left mouse button
        if (button == 0 && dragBox != null && selectedElements.isEmpty()) {
            dragBox.setWidth((float) (mouseX - dragBox.getX()));
            dragBox.setHeight((float) (mouseY - dragBox.getY()));
            isDragging = false;
            selectElementsInRectangle(dragBox);
            dragBox = null;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && dragBox != null && isDragging && selectedElements.isEmpty()) {
            dragBox.setWidth((float) (mouseX - dragBox.getX()));
            dragBox.setHeight((float) (mouseY - dragBox.getY()));
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }


    @SubscribeEvent
    public void keyPressed(KeyPressedEvent keyPressedEvent) {
        if (keyPressedEvent.getKey() == GLFW.GLFW_KEY_DELETE || keyPressedEvent.getKey() == GLFW.GLFW_KEY_BACKSPACE) {
            for (int i = 0; i < HudManager.INSTANCE.hudElements.size(); i++) {
                if (HudManager.INSTANCE.hudElements.get(i).selected) {
                    HudElement hudElement = HudManager.INSTANCE.hudElements.get(i);
                    HudManager.INSTANCE.removeHudElement(hudElement);
                    HudCategoryPane.INSTANCE.getHudElementButton(hudElement).updateCount();
                    i--;
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean copied = false;
        for (HudElement element : HudManager.INSTANCE.hudElements) {
            if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) {
                element.shiftDown = true;
            }

            if (element.selected && isCopy(keyCode) && !copied) {
                element.saveSettingsToMap(copiedSettings);
                copied = true;
                continue;
            }

            if (element.selected && isPaste(keyCode) && !copiedSettings.isEmpty()) {
                for (SettingGroup settingGroup : element.settingGroups) {
                    for (Setting<?> setting : settingGroup.getSettings()) {
                        if (!setting.shouldSaveAndLoad()) continue;

                        setting.loadFromFile(copiedSettings);
                    }
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (Screen.hasShiftDown()) {
            if (!isScrolling) {
                fadeAnimation.startFading(true);
            }
            isScrolling = true;
            HudElement.setSnapSize(HudElement.getSnapSize() + MathHelper.ceil(verticalAmount));
            scrollTimer.resetTimer();
        } else {
            if (isScrolling && fadeAnimation.getInterpolatedAlpha() > 0.7f) {
                fadeAnimation.startFading(false);
            }
            isScrolling = false;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) {
            for (HudElement element : HudManager.INSTANCE.hudElements) {
                element.shiftDown = false;
            }
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
