package dev.heliosclient.hud;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Class for creating HudElements for HUD
 *
 * <h4>Position is stored using this method:</h4><br>
 * There are 3 imaginary lines on each axis. For y-axis, one is on top of the screen, center, bottom. Similarly, for x-axis they are placed on left, center and right.<br>
 * The HUD element stores on each axis to which line it is closest to. This is stored in the posX and posY variable. Element also stores the distance to this line in pixels in the distanceX and distance Y variables. These are capped so the elements can`t go off the screen. These values aren't changed at all while not dragging ensuring no shifts happening.<br>
 * This approach ensures that when resizing the elements on screen don't overlap much and don't shift around. unless they are touched in the editor.
 *
 * <h4>Warning: Makes sure the element is centered along the x and y position. If this is not met they will go off-screen and hit-boxes will be broken.</h4>
 */
public class HudElement {
    public String name;
    public String description;
    public int height = FontManager.fontSize;
    public int width = 10;
    public int x = 90;
    public int y = 90;
    public boolean dragging;
    public int posY = 0;
    public int posX = 0;
    public int distanceY = 90;
    public int distanceX = 90;
    public boolean selected = false;
    public boolean draggable = true;
    public boolean renderOutLineBox = true;
    int startX, startY;

    public HudElement(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Method for rendering in the editor. You probably shouldn't override this.
     *
     * @param drawContext
     * @param textRenderer
     * @param mouseX
     * @param mouseY
     */
    public void renderEditor(DrawContext drawContext, TextRenderer textRenderer, int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX - startX;
            y = mouseY - startY;

            //Get right line to align to
            if (drawContext.getScaledWindowHeight() / 3 > y) {
                posY = 0;
            } else if ((drawContext.getScaledWindowHeight() / 3) * 2 > y) {
                posY = 1;
            } else {
                posY = 2;
            }

            if (drawContext.getScaledWindowWidth() / 3 > x) {
                posX = 0;
            } else if ((drawContext.getScaledWindowWidth() / 3) * 2 > x) {
                posX = 1;
            } else {
                posX = 2;
            }

            //Calculate and store distances from the lines
            if (posX == 0) {
                distanceX = Math.max(x, 0);
            } else if (posX == 1) {
                distanceX = drawContext.getScaledWindowWidth() / 2 - x;
            } else {
                distanceX = Math.max(drawContext.getScaledWindowWidth() - x, this.width / 2);
            }

            if (posY == 0) {
                distanceY = Math.max(y, 0);
            } else if (posY == 1) {
                distanceY = drawContext.getScaledWindowHeight() / 2 - y;
            } else {
                distanceY = Math.max(drawContext.getScaledWindowHeight() - y, this.height / 2);
            }
        }

        //Move to right position according to pos and distance variables
        if (posX == 0) {
            x = Math.max(Math.min(distanceX, drawContext.getScaledWindowWidth() - width / 2), width / 2);
        } else if (posX == 1) {
            x = Math.max(Math.min(drawContext.getScaledWindowWidth() / 2 - distanceX, drawContext.getScaledWindowWidth() - width / 2), width / 2);
        } else {
            x = Math.max(Math.min(drawContext.getScaledWindowWidth() - distanceX, drawContext.getScaledWindowWidth() - width / 2), width / 2);
        }

        if (posY == 0) {
            y = Math.max(Math.min(distanceY, drawContext.getScaledWindowHeight() - height / 2), height / 2);
        } else if (posY == 1) {
            y = Math.max(Math.min(drawContext.getScaledWindowHeight() / 2 - distanceY, drawContext.getScaledWindowHeight() - height / 2), height / 2);
        } else {
            y = Math.max(Math.min(drawContext.getScaledWindowHeight() - distanceY, drawContext.getScaledWindowHeight() - height / 2), height / 2);
        }

        //Draw outline when selected
        if (this.selected && renderOutLineBox) {
            if (dragging) {
                //Draw X and Y lines when centered
                if (distanceX == 0 && posX == 1) {
                    Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), (float) drawContext.getScaledWindowWidth() / 2 - 1, 0, 2, drawContext.getScaledWindowHeight(), 0xFFFF0000);
                }
                if (distanceY == 0 && posY == 1) {
                    Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), 0, (float) drawContext.getScaledWindowHeight() / 2 - 1, drawContext.getScaledWindowWidth(), 2, 0xFF00FF00);
                }
            }

            Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x - 1 - (float) width / 2, y - 1 - (float) height / 2, width + 2, height + 2, 0.4f, 0xFFFFFFFF);

        }
        //Set default height value
        this.height = Math.round(Renderer2D.getStringHeight());

        //Renders element
        renderElement(drawContext, textRenderer);
    }

    /**
     * Rendering outside of editor.
     *
     * @param drawContext
     * @param textRenderer
     */
    public void render(DrawContext drawContext, TextRenderer textRenderer) {
        //Skip if in F3 menu
        if (HeliosClient.MC.options.debugEnabled) return;

        //Move to proper position
        if (posY == 0) {
            y = Math.max(Math.min(distanceY, drawContext.getScaledWindowHeight() - height / 2), height / 2);
        } else if (posY == 1) {
            y = Math.max(Math.min(drawContext.getScaledWindowHeight() / 2 - distanceY, drawContext.getScaledWindowHeight() - height / 2), height / 2);
        } else {
            y = Math.max(Math.min(drawContext.getScaledWindowHeight() - distanceY, drawContext.getScaledWindowHeight() - height / 2), height / 2);
        }

        if (posX == 0) {
            x = Math.max(Math.min(distanceX, drawContext.getScaledWindowWidth() - width / 2), width / 2);
        } else if (posX == 1) {
            x = Math.max(Math.min(drawContext.getScaledWindowWidth() / 2 - distanceX, drawContext.getScaledWindowWidth() - width / 2), width / 2);
        } else {
            x = Math.max(Math.min(drawContext.getScaledWindowWidth() - distanceX, drawContext.getScaledWindowWidth() - width / 2), width / 2);
        }

        //Render element
        renderElement(drawContext, textRenderer);
    }

    /**
     * Rendering function. Put everything you want to render here. Don't forget to set height and width.
     *
     * @param drawContext
     * @param textRenderer
     */
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
    }

    /**
     * Called on load.
     */
    public void onLoad() {
    }

    /**
     * Called when setting gets changed.
     */
    public void onSettingChange() {
    }

    /**
     * Mouse click event.
     *
     * @param mouseX
     * @param mouseY
     * @param button
     */
    public void mouseClicked(double mouseX, double mouseY, int button) {
        //Start dragging
        if (selected && button == 0 && draggable) {
            startX = (int) (mouseX - x);
            startY = (int) (mouseY - y);
            dragging = true;
        }
    }

    /**
     * @param mouseX
     * @param mouseY
     * @return if mouse is hovered over this element
     */
    public boolean hovered(double mouseX, double mouseY) {
        return mouseX > x - (double) width / 2 && mouseX < x + (double) width / 2 && mouseY > y - (double) height / 2 && mouseY < y + (double) height / 2;
    }

    /**
     * Mouse released event.
     *
     * @param mouseX
     * @param mouseY
     * @param button
     */
    public void mouseReleased(double mouseX, double mouseY, int button) {
        //Stop dragging
        dragging = false;
    }
}
