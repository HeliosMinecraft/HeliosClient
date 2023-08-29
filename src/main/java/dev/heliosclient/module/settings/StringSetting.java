package dev.heliosclient.module.settings;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class StringSetting extends Setting {
    public String value;
    private boolean focused = false;
    private int cursorPosition = 0;
    private int selectionPosition = 0;
    String description;
    String displayValue;

    public StringSetting(String name,String description, String value) {
        this.name = name;
        this.value = value;
        this.displayValue=value;
        this.description=description;
    }

    private int scrollOffset = 0;

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor(), false);
        Renderer2D.drawOutlineBox(drawContext,x + 122,y + 4,54,14,1,focused ? Color.WHITE.getRGB() : Color.DARK_GRAY.getRGB());
        Renderer2D.drawRectangle(drawContext,x + 123,y + 5,52,12,Color.black.getRGB());

        scrollOffset = Math.max(0, Math.min(scrollOffset, value.length()));
        cursorPosition = Math.max(0, Math.min(cursorPosition, value.length()));
        int maxWidth = 50;
        if(scrollOffset>0 || cursorPosition>0) {
            displayValue = value.substring(scrollOffset, cursorPosition);
        }
        while (textRenderer.getWidth(displayValue) > maxWidth && scrollOffset < cursorPosition) {
            scrollOffset++;
            displayValue = value.substring(scrollOffset,cursorPosition);
        }
        drawContext.drawText(textRenderer,Text.literal(displayValue),x + 125,y + 7,focused ? 0xFFFFFFFF : 0xFFAAAAAA,false);
        if (focused) {
            int cursorX = x + 125 + textRenderer.getWidth(displayValue.substring(0,cursorPosition - scrollOffset));
            Renderer2D.drawRectangle(drawContext,cursorX,y + 7,1,textRenderer.fontHeight,0xFFFFFFFF);
        }
    }


    @Override
    public void keyPressed(int keyCode,int scanCode,int modifiers) {
        super.keyPressed(keyCode,scanCode,modifiers);
        if (focused) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (!value.isEmpty() && cursorPosition > 0) {
                        value = value.substring(0, cursorPosition - 1) + value.substring(cursorPosition);
                            cursorPosition--;
                    }
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    if (!value.isEmpty() && cursorPosition < value.length()) {
                        value = value.substring(0, cursorPosition) + value.substring(cursorPosition + 1);
                    }
                }
                case GLFW.GLFW_KEY_LEFT -> {
                    if (cursorPosition > 0) {
                        cursorPosition--;
                        if (cursorPosition < scrollOffset) {
                            scrollOffset = Math.max(0, cursorPosition - 4);
                        }
                    }
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    if (cursorPosition < value.length()) {
                        cursorPosition++;
                        int maxWidth = 50;
                        String displayValue = value.substring(scrollOffset,cursorPosition);
                        while (HeliosClient.MC.textRenderer.getWidth(displayValue) > maxWidth && scrollOffset < cursorPosition) {
                            scrollOffset++;
                            displayValue = value.substring(scrollOffset,cursorPosition);
                        }
                    }
                }
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> focused = false;
            }
            if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
                char chr = (char) (keyCode - GLFW.GLFW_KEY_A + 'a');
                if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0 || (modifiers & GLFW.GLFW_MOD_CAPS_LOCK) != 0) {
                    chr = Character.toUpperCase(chr);
                }
                value = value.substring(0, cursorPosition) + chr + value.substring(cursorPosition);
                cursorPosition++;
            } else if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
                char chr = (char) (keyCode - GLFW.GLFW_KEY_0 + '0');
                value = value.substring(0, cursorPosition) + chr + value.substring(cursorPosition);
                cursorPosition++;
            } else if (keyCode == GLFW.GLFW_KEY_SPACE) {
                value = value.substring(0, cursorPosition) + ' ' + value.substring(cursorPosition);
                cursorPosition++;
            }
        }
    }



    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
        drawContext.drawText(textRenderer, Text.literal(name), x + 3, y + 5, ColorManager.INSTANCE.defaultTextColor(), false);
        Renderer2D.drawRectangle(drawContext,x + moduleWidth - 14,y + 4,10,10,focused ? 0xFF55FFFF : 0xFF222222);
        String displayValue = value;
        int maxWidth = moduleWidth - 14 - 3;
        while (textRenderer.getWidth(displayValue) > maxWidth && displayValue.length() > 0) {
            displayValue = displayValue.substring(0, displayValue.length() - 1);
        }
        drawContext.drawText(textRenderer,Text.literal(displayValue),x + moduleWidth,y + 5,focused ? 0xFFFFFFFF : 0xFFAAAAAA,false);
        if (focused) {
            int cursorX = x + moduleWidth + textRenderer.getWidth(displayValue.substring(0,cursorPosition));
            Renderer2D.drawRectangle(drawContext,cursorX,y + 5,1,textRenderer.fontHeight,0xFFFFFFFF);
        }
    }



    @Override
    public void mouseClicked(double mouseX,double mouseY,int button) {
        super.mouseClicked(mouseX,mouseY,button);
        focused = hovered((int) mouseX, (int) mouseY);
    }

}

