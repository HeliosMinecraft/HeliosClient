package dev.heliosclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InputBox {
    public int x, y, width, height;
    private String value;
    private List<String> textSegments;
    private boolean focused = false;
    private int cursorPosition = 0;
    private int selectionStart = 0;
    private int selectionEnd = 0;
    private int scrollOffset = 0;
    private int characterLimit = 0;
    private boolean selecting = false;
    private boolean selectedAll = false;

    public InputBox(int width, int height, String value, int characterLimit) {
        this.width = width;
        this.height = height;
        this.value = value;
        this.characterLimit = characterLimit;
        this.textSegments = new ArrayList<>();
    }

    public void setText(String text) {
        this.value = text;
        this.textSegments.clear();

        int maxWidth = width - 12;
        int startIndex = 0;
        while (startIndex < text.length()) {
            int endIndex = startIndex + 1;
            while (endIndex < text.length() && MinecraftClient.getInstance().textRenderer.getWidth(text.substring(startIndex, endIndex)) <= maxWidth) {
                endIndex++;
            }
            this.textSegments.add(text.substring(startIndex, endIndex));
            startIndex = endIndex;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        focused = (mouseX >= x + 1 && mouseX <= x + 3 + width && mouseY >= (y + 10 + MinecraftClient.getInstance().textRenderer.fontHeight) && mouseY <= (y + 12 + MinecraftClient.getInstance().textRenderer.fontHeight + height));
        cursorPosition = value.length();
        return focused;
    }

    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        setText(value);

        this.x = x;
        this.y = y;
        Renderer2D.drawOutlineBox(drawContext, x + 1, y + 10 + textRenderer.fontHeight, width + 2, height + 2, 1, focused ? Color.WHITE.getRGB() : Color.DARK_GRAY.getRGB());
        Renderer2D.drawRectangle(drawContext, x + 2, y + textRenderer.fontHeight + 11, width, height, Color.black.getRGB());

        if (focused) {
            scrollOffset = Math.max(0, Math.min(scrollOffset, value.length()));
            cursorPosition = Math.max(0, Math.min(cursorPosition, value.length()));

            // Find the segment that contains the cursor
            int segmentIndex = 0;
            int segmentStartIndex = 0;
            if (!textSegments.isEmpty()) {
                for (int i = 0; i < textSegments.size(); i++) {
                    if (cursorPosition >= segmentStartIndex && cursorPosition <= segmentStartIndex + textSegments.get(i).length()) {
                        segmentIndex = i;
                        break;
                    }
                    segmentStartIndex += textSegments.get(i).length();
                }

                // Display the segment that contains the cursor
                String displayValue = textSegments.get(segmentIndex);
                drawContext.drawText(textRenderer,
                        Text.literal(displayValue),
                        x + 5,
                        y + height / 3 + 10 + textRenderer.fontHeight,
                        0xFFFFFFFF,
                        false);

                // Draw the cursor
                int cursorX = x + 5 + textRenderer.getWidth(displayValue.substring(0, cursorPosition - segmentStartIndex));
                Renderer2D.drawRectangle(drawContext,
                        cursorX,
                        y + height / 3 + 10 + textRenderer.fontHeight,
                        1,
                        textRenderer.fontHeight - 1,
                        Color.LIGHT_GRAY.getRGB());
            }
        } else {
            // Display the first segment of the text
            String displayValue = !textSegments.isEmpty() ? textSegments.get(0) : "";
            drawContext.drawText(textRenderer,Text.literal(displayValue),x + 5,y + height / 3 + 10 + textRenderer.fontHeight,0xFFAAAAAA,false);
        }

        // Draw selection box
        if (focused && selecting && selectionStart != selectionEnd) {
            int startX = x + 4 + textRenderer.getWidth(value.substring(0, Math.min(selectionStart, value.length())));
            int endX = x + 4 + textRenderer.getWidth(value.substring(0, Math.min(selectionEnd, value.length())));
            if (endX > x + width) {
                endX = x + width;
            }
            Renderer2D.drawRectangle(drawContext, startX, y + height / 3 + 9 + textRenderer.fontHeight, endX - startX + 1, textRenderer.fontHeight, new Color(0, 166, 255, 64).getRGB());
        }
    }


    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (focused && canWrite()) {
            if (Screen.isSelectAll(keyCode)) {
                selecting = true;
                selectedAll = true;
                selectionStart = 0;
                setCursorPos(value.length());
                selectionEnd = cursorPosition;
            }
            if (selectedAll && (GLFW.GLFW_KEY_DELETE == keyCode || GLFW.GLFW_KEY_BACKSPACE == keyCode)) {
                selectionStart = cursorPosition;
                selectionEnd = cursorPosition;
                setText("");
                selectedAll = false;
                selecting = false;
            }
            if (Screen.isCopy(keyCode)) {
                selectedAll = false;
                selecting = false;
                //selectionStart = cursorPosition;
                selectionEnd = cursorPosition;
                MinecraftClient.getInstance().keyboard.setClipboard(this.getTextToCopy());
            }
            if (Screen.isPaste(keyCode)) {
                selectedAll = false;
                selecting = false;
                selectionStart = cursorPosition;
                selectionEnd = cursorPosition;
                paste();
            }
            if (Screen.isCut(keyCode)) {
                selectedAll = false;
                //electionStart = cursorPosition;
                selectionEnd = cursorPosition;
                MinecraftClient.getInstance().keyboard.setClipboard(this.getTextToCopy());
                setText("");
            }
            if (!selecting) {
                selectedAll = false;
                selectionStart = cursorPosition;
                selectionEnd = cursorPosition;
            }

            switch (keyCode) {
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (!value.isEmpty() && cursorPosition > 0) {
                        if (selecting && selectionEnd > 0) {
                            selectionEnd--;
                        }
                        value = value.substring(0, cursorPosition - 1) + value.substring(cursorPosition);
                        setText(value);
                        cursorPosition--;
                    }
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    if (!value.isEmpty() && cursorPosition < value.length()) {
                        value = value.substring(0, cursorPosition) + value.substring(cursorPosition + 1);
                        setText(value);
                    }
                }

                case GLFW.GLFW_KEY_LEFT -> {
                    moveCursor(-1);
                    if (Screen.hasShiftDown()) {
                        selecting = true;
                        if (cursorPosition > 0) {
                            selectionEnd = cursorPosition;
                        }
                    }
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    moveCursor(1);
                    if (Screen.hasShiftDown()) {
                        selecting = true;
                        if (cursorPosition < value.length()) {
                            selectionEnd = cursorPosition;
                        }
                    }
                }
                case GLFW.GLFW_KEY_ENTER,
                        GLFW.GLFW_KEY_KP_ENTER -> focused = false;
            }
        }
    }

    public void keyReleased(int keyCode, int scanCode, int modifiers) {
     /*   if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            selecting = false;
        }

      */
    }

    private void insertCharacter(char chr) {
        if (value.length() <= characterLimit) {
            value = value.substring(0, cursorPosition) + chr + value.substring(cursorPosition);
            setText(value);
            cursorPosition++;
        }
    }

    public void paste() {
        // Get the text from the system clipboard
        String clipboardText = MinecraftClient.getInstance().keyboard.getClipboard();

        // Insert the clipboard text into the value field at the current cursor position
        value = value.substring(0, cursorPosition) + clipboardText + value.substring(cursorPosition);
        setText(value);

        // Move the cursor to the end of the pasted text
        cursorPosition += clipboardText.length();
    }

    public void moveCursor(int offset) {
        this.setCursorPos(this.cursorPosition + offset);
    }

    public void charTyped(char chr, int modifiers) {
        if (focused) {
            insertCharacter(chr);
        }
    }

    public void setCursorPos(int pos) {
        this.cursorPosition = MathHelper.clamp(pos, 0, this.value.length());
    }

    public boolean canWrite() {
        return true; // You can modify this method to add conditions for when the user can write to the input box
    }

    public String getTextToCopy() {
        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);
        return value.substring(start, end);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = scrollOffset;
    }

    public List<String> getTextSegments() {
        return textSegments;
    }

    public void setTextSegments(List<String> textSegments) {
        this.textSegments = textSegments;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getCharacterLimit() {
        return characterLimit;
    }

    public void setCharacterLimit(int characterLimit) {
        this.characterLimit = characterLimit;
    }

    public String getValue() {
        return value;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

