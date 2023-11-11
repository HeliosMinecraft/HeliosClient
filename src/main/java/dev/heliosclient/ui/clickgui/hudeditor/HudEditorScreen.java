package dev.heliosclient.ui.clickgui.hudeditor;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.KeyPressedEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.hud.HudElement;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.HudManager;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.navbar.NavBar;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class HudEditorScreen extends Screen implements Listener {
    public HudCategoryPane pane = new HudCategoryPane();

    public HudEditorScreen() {
        super(Text.of("Hud editor"));
        EventManager.register(this);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderBackground(drawContext);

        float thickness = 0.5f;
        Matrix4f positionMatrix = drawContext.getMatrices().peek().getPositionMatrix();
        float threshold = 1;

        // Check for nearby HudElements
        for (int i = 0; i < HudManager.INSTANCE.hudElements.size(); i++) {
            HudElement element1 = HudManager.INSTANCE.hudElements.get(i);
            if (!element1.selected) {
                continue;
            }
            for (int j = 0; j < HudManager.INSTANCE.hudElements.size(); j++) {
                if (i == j) {
                    continue; // Skip comparison with itself
                }
                HudElement element2 = HudManager.INSTANCE.hudElements.get(j);

                // If the left sides of the HudElements are aligned
                if (Math.abs(element1.hitbox.getX() - element2.hitbox.getX()) < threshold) {
                    // Draw a vertical line at the left edge
                    Renderer2D.drawVerticalLine(positionMatrix, element2.hitbox.getX(), 0, this.height, thickness, Color.WHITE.getRGB());
                }
                // If the right sides of the HudElements are aligned
                if (Math.abs((element1.hitbox.getX() + element1.hitbox.getWidth()) - (element2.hitbox.getX() + element2.hitbox.getWidth())) < threshold) {
                    // Draw a vertical line at the right edge
                    Renderer2D.drawVerticalLine(positionMatrix, (element2.hitbox.getX() + element2.hitbox.getWidth()), 0, this.height, thickness, Color.WHITE.getRGB());
                }
                // If the top sides of the HudElements are aligned
                if (Math.abs(element1.hitbox.getY() - element2.hitbox.getY()) < threshold) {
                    // Draw a horizontal line at the top edge
                    Renderer2D.drawHorizontalLine(positionMatrix, 0, this.width, element2.hitbox.getY(), thickness, Color.WHITE.getRGB());
                }
                // If the bottom sides of the HudElements are aligned
                if (Math.abs((element1.hitbox.getY() + element1.hitbox.getHeight()) - (element2.hitbox.getY() + element2.hitbox.getHeight())) < threshold) {
                    // Draw a horizontal line at the bottom edge
                    Renderer2D.drawHorizontalLine(positionMatrix, 0, this.width, (element2.hitbox.getY() + element2.hitbox.getHeight()), thickness, Color.WHITE.getRGB());
                }

                // If the left side of one HudElement is touching the right side of another HudElement
                if (Math.abs(element1.hitbox.getX() - (element2.hitbox.getX() + element2.hitbox.getWidth())) < threshold) {
                    // Draw a vertical line at the right edge of the first HudElement
                    Renderer2D.drawVerticalLine(positionMatrix, element1.hitbox.getX(), 0, this.height, thickness, Color.WHITE.getRGB());
                }
                // If the right side of one HudElement is touching the left side of another HudElement
                if (Math.abs((element1.hitbox.getX() + element1.hitbox.getWidth()) - element2.hitbox.getX()) < threshold) {
                    // Draw a vertical line at the left edge of the first HudElement
                    Renderer2D.drawVerticalLine(positionMatrix, (element1.hitbox.getX() + element1.hitbox.getWidth()), 0, this.height, thickness, Color.WHITE.getRGB());
                }
                // If the top side of one HudElement is touching the bottom side of another HudElement
                if (Math.abs(element1.hitbox.getY() - (element2.hitbox.getY() + element2.hitbox.getHeight())) < threshold) {
                    // Draw a horizontal line at the bottom edge of the first HudElement
                    Renderer2D.drawHorizontalLine(positionMatrix, 0, this.width, element1.hitbox.getY(), thickness, Color.WHITE.getRGB());
                }
                // If the bottom side of one HudElement is touching the top side of another HudElement
                if (Math.abs((element1.hitbox.getY() + element1.hitbox.getHeight()) - element2.hitbox.getY()) < threshold) {
                    // Draw a horizontal line at the top edge of the first HudElement
                    Renderer2D.drawHorizontalLine(positionMatrix, 0, this.width, (element1.hitbox.getY() + element1.hitbox.getHeight()), thickness, Color.WHITE.getRGB());
                }
            }
        }

        HudManager.INSTANCE.renderEditor(drawContext, textRenderer, mouseX, mouseY);
        HudCategoryPane.INSTACE.render(drawContext, textRenderer, mouseX, mouseY, delta);
        NavBar.navBar.render(drawContext, textRenderer, mouseX, mouseY);
    }

    @Override
    public boolean shouldPause() {
        return ClickGUI.pause;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        NavBar.navBar.mouseClicked((int) mouseX, (int) mouseY, button);
        HudCategoryPane.INSTACE.mouseClicked(mouseX, mouseY, button);

        if (button == 0) {
            int lastHoveredIndex = -1;
            for (int i = HudManager.INSTANCE.hudElements.size() - 1; i >= 0; i--) {
                if (HudManager.INSTANCE.hudElements.get(i).hovered(mouseX, mouseY)) {
                    HudManager.INSTANCE.hudElements.get(i).selected = true;
                    lastHoveredIndex = i;
                    break;
                }
            }

            for (int i = 0; i < HudManager.INSTANCE.hudElements.size(); i++) {
                if (i != lastHoveredIndex) {
                    HudManager.INSTANCE.hudElements.get(i).selected = false;
                }
                HudManager.INSTANCE.hudElements.get(i).mouseClicked(mouseX, mouseY, button);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        HudCategoryPane.INSTACE.mouseReleased(mouseX, mouseY, button);
        for (HudElement element : HudManager.INSTANCE.hudElements) {
            element.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
