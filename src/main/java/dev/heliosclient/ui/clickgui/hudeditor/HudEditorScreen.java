package dev.heliosclient.ui.clickgui.hudeditor;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.input.KeyPressedEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.HudManager;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.gui.Hitbox;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.fontutils.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HudEditorScreen extends Screen implements Listener {
    private final List<HudElement> selectedElements = new ArrayList<>();
    float thickness = 0.5f;
    float threshold = 1;
    // Variables to track the drag state and initial position
    private boolean isDragging = false;
    private Hitbox dragBox = null;
    public static HudEditorScreen INSTANCE = new HudEditorScreen();


    private HudEditorScreen() {
        super(Text.of("Hud editor"));
        EventManager.register(this);
        dragBox = new Hitbox(0, 0, 0, 0);
    }


    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext, mouseX, mouseY, delta);
        if(HeliosClient.CLICKGUI.ScreenHelp.value) {
            float fontHeight = Renderer2D.getCustomStringHeight(FontRenderers.Super_Small_fxfontRenderer);
            FontRenderers.Super_Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Left Click - Select Element", 2, drawContext.getScaledWindowHeight() - (4 * fontHeight) - 4 * 2, -1);
            FontRenderers.Super_Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Right Click - Open Settings", 2, drawContext.getScaledWindowHeight() - (3 * fontHeight) - 3 * 2, -1);
            FontRenderers.Super_Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Delete / Backspace - Remove Element", 2, drawContext.getScaledWindowHeight() - (2 * fontHeight) - 2 * 2, -1);
            FontRenderers.Super_Small_fxfontRenderer.drawString(drawContext.getMatrices(), "Hold Shift Key - Enable Snapping", 2, drawContext.getScaledWindowHeight() - fontHeight - 2, -1);
        }
        checkAlignmentAndDrawLines(drawContext);

        if (dragBox != null && !HudCategoryPane.INSTANCE.hoveredComplete(mouseX,mouseY)) {
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(0,0,-0.0D);
            drawContext.fill(Math.round(dragBox.getX()), Math.round(dragBox.getY()), Math.round(dragBox.getX() + dragBox.getWidth()), Math.round(dragBox.getY() + dragBox.getHeight()), new Color(255, 255, 255, 45).getRGB());
            drawContext.drawBorder(Math.round(dragBox.getX() - 1), Math.round(dragBox.getY() - 1), Math.round(dragBox.getWidth() + 1), Math.round(dragBox.getHeight() + 1), new Color(255, 255, 255, 155).getRGB());
            drawContext.getMatrices().pop();
        }

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
                checkAndDrawVerticalLine(drawContext, element1.hitbox.getLeft(), element2.hitbox.getLeft(), element2.hitbox.getX());

                checkAndDrawVerticalLine(drawContext, element1.hitbox.getRight(), element2.hitbox.getRight(), element2.hitbox.getX() + element2.hitbox.getWidth());

                checkAndDrawHorizontalLine(drawContext, element1.hitbox.getTop(), element2.hitbox.getTop(), element2.hitbox.getY());

                checkAndDrawHorizontalLine(drawContext, element1.hitbox.getBottom(), element2.hitbox.getBottom(), element2.hitbox.getY() + element2.hitbox.getHeight());
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
            if (!selectedElements.isEmpty() && !isDragging && dragBox == null && !HudCategoryPane.INSTANCE.hoveredComplete(mouseX, mouseY)) {
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
                    if(button == 1){
                        HeliosClient.MC.setScreen(new HudEditorSettingScreen(HudManager.INSTANCE.hudElements.get(i),180,180));
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
            if (lastHoveredIndex == -1 && selectedElements.isEmpty() && !HudCategoryPane.INSTANCE.hoveredComplete(mouseX,mouseY) && dragBox == null) {
                isDragging = true;
                int dragStartX = (int) mouseX;
                int dragStartY = (int) mouseY;
                dragBox = new Hitbox(dragStartX, dragStartY, 0, 0);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void selectElementsInRectangle(Hitbox dragBox) {
        for (HudElement element : HudManager.INSTANCE.hudElements) {
            if (element.hitbox.intersects(dragBox)) {
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
                    HudManager.INSTANCE.removeHudElement(HudManager.INSTANCE.hudElements.get(i));
                    i--;
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) {
            for (HudElement element : HudManager.INSTANCE.hudElements) {
                element.shiftDown = true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
