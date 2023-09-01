package dev.heliosclient.hud;

import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class HudElement {
    public String name;
    public String description;
    public int height = 8;
    public int width = 10;
    public int x = 90;
    public int y = 90;
    public boolean dragging;
    int startX, startY;
    public int posY = 0;
    public int posX = 0;
    public int distanceY = 90;
    public int distanceX = 90;

    public boolean selected = false;
    public boolean draggable = true;
    public boolean renderOutLineBox = true;

    public HudElement(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void renderEditor(DrawContext drawContext, TextRenderer textRenderer, int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX - startX;
            y = mouseY - startY;

            if (drawContext.getScaledWindowHeight()/3 > y) {
                posY = 0;
            } else if ((drawContext.getScaledWindowHeight()/3)*2 > y) {
                posY = 1;
            } else {
                posY = 2;
            }

            if (drawContext.getScaledWindowWidth()/3 > x) {
                posX = 0;
            } else if ((drawContext.getScaledWindowWidth()/3)*2 > x) {
                posX = 1;
            } else {
                posX = 2;
            }

            if (posX ==0)
            {
                distanceX = Math.max(x, 0);
            }
            else if (posX == 1)
            {
                distanceX = drawContext.getScaledWindowWidth()/2-x;
            }
            else {
                distanceX = Math.max(drawContext.getScaledWindowWidth()-x, this.width/2);
            }

            if (posY ==0)
            {
                distanceY = Math.max(y, 0);
            }
            else if (posY == 1)
            {
                distanceY = drawContext.getScaledWindowHeight()/2-y;
            }
            else {
                distanceY = Math.max(drawContext.getScaledWindowHeight()-y, this.height/2);
            }

        }
        if (posX == 0) {
            x = Math.max(Math.min(distanceX, drawContext.getScaledWindowWidth()-width/2), width/2);
        } else if (posX == 1) {
            x = Math.max(Math.min(drawContext.getScaledWindowWidth()/2- distanceX, drawContext.getScaledWindowWidth()-width/2), width/2);
        } else {
            x = Math.max(Math.min(drawContext.getScaledWindowWidth()- distanceX, drawContext.getScaledWindowWidth()-width/2), width/2);
        }

        if (posY == 0) {
            y = Math.max(Math.min(distanceY, drawContext.getScaledWindowHeight()-height/2), height/2);
        } else if (posY == 1) {
            y = Math.max(Math.min(drawContext.getScaledWindowHeight()/2- distanceY, drawContext.getScaledWindowHeight()-height/2), height/2);
        } else {
            y = Math.max(Math.min(drawContext.getScaledWindowHeight()- distanceY, drawContext.getScaledWindowHeight()-height/2), height/2);
        }

        if (this.selected && renderOutLineBox) {
            if (dragging) {
            if (distanceX == 0 && posX == 1) {
                Renderer2D.drawRectangle(drawContext, drawContext.getScaledWindowWidth()/2-1, 0, 2, drawContext.getScaledWindowHeight(), 0xFFFF0000);
            }
            if (distanceY == 0 && posY == 1) {
                Renderer2D.drawRectangle(drawContext, 0, drawContext.getScaledWindowHeight()/2-1, drawContext.getScaledWindowWidth(), 2, 0xFF00FF00);
            }
            }

            Renderer2D.drawOutlineBox(drawContext,x-1 - width/2,y-1 - height/2,width + 2,height,1,0xFFFFFFFF);

           // drawContext.fill(x-1 - width/2, y-1 - height/2, x- width/2, y+height/2+1, 0xFFFFFFFF);
            //drawContext.fill(x-1 - width/2, y-1 -height/2, x+width+1- width/2 , y-height/2, 0xFFFFFFFF);
             //drawContext.fill(x+width - width/2, y-1 - height/2, x+width+1- width/2, y+height/2+1, 0xFFFFFFFF);
               //drawContext.fill(x-1 - width/2, y+height/2 , x+width+1- width/2, y+height/2+1, 0xFFFFFFFF);
        }

        renderElement(drawContext, textRenderer);
    }

    public void render(DrawContext drawContext, TextRenderer textRenderer) {

        if (posY == 0) {
            y = Math.max(Math.min(distanceY, drawContext.getScaledWindowHeight()-height/2), height/2);
        } else if (posY == 1) {
            y = Math.max(Math.min(drawContext.getScaledWindowHeight()/2- distanceY, drawContext.getScaledWindowHeight()-height/2), height/2);
        } else {
            y = Math.max(Math.min(drawContext.getScaledWindowHeight()- distanceY, drawContext.getScaledWindowHeight()-height/2), height/2);
        }

        if (posX == 0) {
            x = Math.max(Math.min(distanceX, drawContext.getScaledWindowWidth()-width/2), width/2);
        } else if (posX == 1) {
            x = Math.max(Math.min(drawContext.getScaledWindowWidth()/2- distanceX, drawContext.getScaledWindowWidth()-width/2), width/2);
        } else {
            x = Math.max(Math.min(drawContext.getScaledWindowWidth()- distanceX, drawContext.getScaledWindowWidth()-width/2), width/2);
        }

        renderElement(drawContext, textRenderer);
    }

    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {}

    public void onLoad() {}
    public void onSettingChange() {}

    public void  mouseClicked(double mouseX, double mouseY, int button) {
        if (selected && button == 0 && draggable) {
            startX = (int) (mouseX - x);
            startY = (int) (mouseY - y);
            dragging = true;
        }
    }

    public boolean hovered(double mouseX, double mouseY) {
        return mouseX > x - (double) width /2 && mouseX < x + (double) width /2 && mouseY > y - (double) height /2 && mouseY < y + (double) height /2;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }
}
