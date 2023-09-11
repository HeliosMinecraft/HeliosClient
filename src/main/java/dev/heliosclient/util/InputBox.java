package dev.heliosclient.util;

import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.CharTypedEvent;
import dev.heliosclient.event.events.KeyPressedEvent;
import dev.heliosclient.event.listener.Listener;
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

public class InputBox implements Listener {
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
        EventManager.register(this);
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


    @SubscribeEvent
    public void keyPressed(KeyPressedEvent event) {
        int keyCode = event.getKey();
        if (focused && canWrite()) {
            if (Screen.isSelectAll(keyCode)) {
                selecting = true;
                selectedAll = true;
                selectionStart = 0;
                selectionEnd = value.length();
            }
            if (selectedAll && (GLFW.GLFW_KEY_DELETE == keyCode || GLFW.GLFW_KEY_BACKSPACE == keyCode)) {
                value = "";
                selectedAll = false;
                selecting = false;
            }
            if (Screen.isCopy(keyCode)) {
                selectedAll = false;
                selecting = false;
                MinecraftClient.getInstance().keyboard.setClipboard(this.getTextToCopy());
                selectionStart=0;
                selectionEnd=0;
            }
            if (Screen.isPaste(keyCode)) {
                selectedAll = false;
                selecting = false;
                paste();
            }
            if (Screen.isCut(keyCode)) {
                selectedAll = false;
                selecting = false;
                MinecraftClient.getInstance().keyboard.setClipboard(this.getTextToCopy());
                value = "";
            }

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
                    moveCursor(-1);
                    if (Screen.hasShiftDown()) {
                        if (!selecting) {
                            selecting = true;
                            selectionEnd = cursorPosition + 1;
                        }
                        selectionStart = cursorPosition;
                    } else {
                        selecting = false;
                    }
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    moveCursor(1);
                    if (Screen.hasShiftDown()) {
                        if (!selecting) {
                            selecting = true;
                            selectionStart = cursorPosition - 1;
                        }
                        selectionEnd = cursorPosition;
                    } else {
                        selecting = false;
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

    public void moveCursor(int offset) {
        this.setCursorPos(this.cursorPosition + offset);
    }

    @SubscribeEvent
    public void charTyped(CharTypedEvent event) {
        char chr = event.getI();
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

    public void paste() {
        // Get the text from the system clipboard
        String clipboardText = MinecraftClient.getInstance().keyboard.getClipboard();

        // If text is selected, replace it with the clipboard text
        // Otherwise, insert the clipboard text at the cursor position
        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);
        if (start != end) {
            StringBuilder builder = new StringBuilder(value);
            builder.replace(start, end, clipboardText);
            value = builder.toString();
            cursorPosition = start + clipboardText.length();
        } else {
            StringBuilder builder = new StringBuilder(value);
            builder.insert(cursorPosition, clipboardText);
            value = builder.toString();
            cursorPosition += clipboardText.length();
        }

        // Clear the selection
        selectionStart = selectionEnd = cursorPosition;
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

