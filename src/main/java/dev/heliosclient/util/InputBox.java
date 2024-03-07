package dev.heliosclient.util;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.input.CharTypedEvent;
import dev.heliosclient.event.events.input.KeyHeldEvent;
import dev.heliosclient.event.events.input.KeyPressedEvent;
import dev.heliosclient.event.events.input.MouseClickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InputBox implements Listener {
    protected final InputMode inputMode;
    public int x, y, width, height;
    protected String value;
    protected List<String> textSegments;
    protected boolean focused = false;
    protected int cursorPosition = 0;
    protected int selectionStart = 0;
    protected int selectionEnd = 0;
    protected int scrollOffset = 0;
    protected int characterLimit;
    protected boolean selecting = false;
    protected boolean selectedAll = false;
    protected Screen screen;

    public InputBox(int width, int height, String value, int characterLimit, InputMode inputMode) {
        this.width = width;
        this.height = height;
        this.value = value;
        this.characterLimit = characterLimit;
        this.textSegments = new ArrayList<>();
        this.inputMode = inputMode;
        EventManager.register(this);
    }

    @SubscribeEvent
    public void keyHeld(KeyHeldEvent event) {
        int keyCode = event.getKey();
        if (focused && canWrite()) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (!value.isEmpty() && cursorPosition > 0) {
                        if (selecting) {
                            value = value.substring(0, selectionStart) + value.substring(selectionEnd);
                            cursorPosition = selectionStart;
                            selecting = false;
                            selectionStart = 0;
                            selectionEnd = 0;
                        } else {
                            value = value.substring(0, cursorPosition - 1) + value.substring(cursorPosition);
                            cursorPosition--;
                        }
                    }
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    if (selecting) {
                        value = value.substring(0, selectionStart) + value.substring(selectionEnd);
                        cursorPosition = selectionStart;
                        selecting = false;
                        selectionStart = 0;
                        selectionEnd = 0;
                    } else if (cursorPosition != value.length()) {
                        value = value.substring(0, cursorPosition) + value.substring(cursorPosition + 1);
                    }
                }
                case GLFW.GLFW_KEY_ENTER,
                        GLFW.GLFW_KEY_KP_ENTER -> {
                    focused = false;
                }
            }
        }
    }

    public void setText(String text) {
        this.value = text;
        this.textSegments.clear();

        int maxWidth = width - 12;
        int startIndex = 0;
        while (startIndex < text.length()) {
            int endIndex = startIndex + 1;
            while (endIndex < text.length() && Renderer2D.getFxStringWidth(text.substring(startIndex, endIndex)) <= maxWidth) {
                endIndex++;
            }
            this.textSegments.add(text.substring(startIndex, endIndex));
            startIndex = endIndex;
        }
    }

    @SubscribeEvent
    public void mouseClicked(MouseClickEvent event) {
        if (screen != null && event.getScreen() == screen) {
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            isFocusedHover(mouseX, mouseY);
            cursorPosition = value.length();
        }
    }

    public boolean isFocusedHover(double mouseX, double mouseY) {
        return focused = (mouseX >= x + 1 && mouseX <= x + 3 + width && mouseY >= (y) && mouseY <= (y + height));
    }


    public void update(int x, int y) {
        this.screen = HeliosClient.MC.currentScreen;
        setText(value);

        this.x = x;
        this.y = y;
    }

    public String displaySegment(DrawContext drawContext, float textY, float textHeight) {
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
            Renderer2D.drawFixedString(drawContext.getMatrices(), (displayValue), x + 5, Renderer2D.isVanillaRenderer() ? textY + 1 : textY, 0xFFFFFFFF);
            displayCursor(drawContext, displayValue, textY, textHeight, segmentStartIndex, 0.5f);
            return displayValue;
        }
        return "";
    }

    public void displayCursor(DrawContext drawContext, String displayValue, float textY, float textHeight, int segmentStartIndex, float cursorWidth) {
        // Draw the cursor
        if (!textSegments.isEmpty()) {
            String strBeforeCursor = displayValue.substring(0, cursorPosition - segmentStartIndex);
            float cursorX = (x + 5.5f + Renderer2D.getFxStringWidth(strBeforeCursor));

            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(),
                    cursorX - 0.3f,
                    textY + 0.5f,
                    cursorWidth,
                    textHeight - 0.5f,
                    ColorUtils.rgbaToInt(150, 150, 150, 255));
        }
    }


    public void displayFirstSegment(DrawContext drawContext, float textY) {
        // Display the first segment of the text
        String displayValue = !textSegments.isEmpty() ? textSegments.get(0) : "";
        Renderer2D.drawFixedString(drawContext.getMatrices(), (displayValue), x + 5, Renderer2D.isVanillaRenderer() ? textY + 1 : textY, 0xFFAAAAAA);
    }

    public void drawSelectionBox(DrawContext drawContext, float textY, float textHeight) {
        // Draw selection box
        if (focused && selecting && selectionStart != selectionEnd) {
            String strBeforeStart = value.substring(0, Math.min(selectionStart, value.length()));
            String strBeforeEnd = value.substring(0, Math.min(selectionEnd, value.length()));
            int startX = (int) (x + 5.5f + Renderer2D.getFxStringWidth(strBeforeStart));
            int endX = (int) (x + 5.5f + Renderer2D.getFxStringWidth(strBeforeEnd));
            if (endX > x + width) {
                endX = x + width;
            }
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), startX, Renderer2D.isVanillaRenderer() ? textY - 1 : textY, endX - startX + 1, textHeight - 1, new Color(0, 166, 255, 64).getRGB());
        }
    }

    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        update(x, y);

        renderBackground(drawContext);

        float textHeight = Renderer2D.getFxStringHeight();
        float textY = y + (height - textHeight) / 2; // Center the text vertically

        if (focused) {
            scrollOffset = Math.max(0, Math.min(scrollOffset, value.length()));
            cursorPosition = Math.max(0, Math.min(cursorPosition, value.length()));
            displaySegment(drawContext, textY, textHeight);

        } else {
            displayFirstSegment(drawContext, textY);
        }


        drawSelectionBox(drawContext, textY, textHeight);
    }

    public void renderBackground(DrawContext drawContext) {
        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x + 1.2f, y - 0.5f, width + 1.5f, height + 1f, 3, 0.5f, focused ? Color.WHITE.getRGB() : Color.DARK_GRAY.getRGB());
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 2, y, width, height, 2, Color.BLACK.getRGB());
    }

    public void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
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
                selectionStart = 0;
                selectionEnd = 0;
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
                    selectedAll = false;
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
                    selectedAll = false;

                }
                case GLFW.GLFW_KEY_ENTER,
                        GLFW.GLFW_KEY_KP_ENTER -> focused = false;
            }
            if (keyCode == GLFW.GLFW_KEY_HOME) {
                if (Screen.hasShiftDown()) {
                    if (!selecting) {
                        selecting = true;
                        selectionStart = 0;
                    }
                    selectionEnd = cursorPosition;
                } else {
                    selecting = false;
                }
                selectedAll = false;
                cursorPosition = 0;
            }
            if (keyCode == GLFW.GLFW_KEY_END) {
                if (Screen.hasShiftDown()) {
                    if (!selecting) {
                        selecting = true;
                        selectionStart = cursorPosition;
                    }
                    selectionEnd = value.length();
                } else {
                    selecting = false;
                }
                cursorPosition = value.length();
            }
        }
    }

    @SubscribeEvent
    public void charTyped(CharTypedEvent event) {
        char chr = event.getI();
        if (focused) {
            switch (inputMode) {
                case DIGITS -> {
                    if (Character.isDigit(chr) || Character.toString(chr).equals(".")) {
                        insertCharacter(chr);
                    } else {
                        AnimationUtils.addErrorToast(ColorUtils.red + "Enter only digits " + ColorUtils.green + "0-9 ", true, 1000);
                    }
                }
                case CHARACTERS -> {
                    if (Character.isLetter(chr)) {
                        insertCharacter(chr);
                    } else {
                        AnimationUtils.addErrorToast(ColorUtils.red + "Enter only letters a-z // A-Z", true, 1000);
                    }
                }
                case CHARACTERS_AND_WHITESPACE -> {
                    if (Character.isLetter(chr) || Character.isWhitespace(chr)) {
                        insertCharacter(chr);
                    } else {
                        AnimationUtils.addErrorToast(ColorUtils.red + "Enter only letters or blank " + ColorUtils.green + " a-z // A-Z //  ", true, 1000);
                    }
                }
                case DIGITS_AND_CHARACTERS_AND_WHITESPACE -> {
                    if (Character.isLetterOrDigit(chr) || Character.isWhitespace(chr)) {
                        insertCharacter(chr);
                    } else {
                        AnimationUtils.addErrorToast(ColorUtils.red + "Enter only digits or letters or blank " + ColorUtils.green + " a-z // A-Z //   // 0-9", true, 1000);
                    }
                }
                case DIGITS_AND_CHARACTERS -> {
                    if (Character.isLetterOrDigit(chr)) {
                        insertCharacter(chr);
                    } else {
                        AnimationUtils.addErrorToast(ColorUtils.red + "Enter only digits or letters" + ColorUtils.green + " a-z // A-Z // 0-9", true, 1000);
                    }
                }
                case DIGITS_AND_CHARACTERS_AND_UNDERSCORE -> {
                    if (Character.isLetterOrDigit(chr) || chr == '_') {
                        insertCharacter(chr);
                    } else {
                        AnimationUtils.addErrorToast(ColorUtils.red + "Enter only digits or letters or underscore" + ColorUtils.green + " a-z // A-Z // 0-9 // _", true, 1000);
                    }
                }
                case ALL -> insertCharacter(chr);
            }
        }
    }


    private void insertCharacter(char chr) {
        if (value.length() <= characterLimit) {
            // Check if text is selected
            if (selecting) {
                // Replace the selected text with the new character
                int start = Math.min(selectionStart, selectionEnd);
                int end = Math.max(selectionStart, selectionEnd);
                StringBuilder builder = new StringBuilder(value);
                builder.replace(start, end, String.valueOf(chr));
                value = builder.toString();
                cursorPosition = start + 1;

                // Clear the selection
                selecting = false;
                selectionStart = selectionEnd = cursorPosition;
            } else if (cursorPosition >= 0 && cursorPosition <= value.length()) {
                // Insert the new character at the cursor position
                value = value.substring(0, cursorPosition) + chr + value.substring(cursorPosition);
                cursorPosition++;
            }
        }
    }

    public void moveCursor(int offset) {
        this.setCursorPos(this.cursorPosition + offset);
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
        StringBuilder builder = new StringBuilder(value);
        if (start == end) {
            builder.insert(cursorPosition, clipboardText);
            value = builder.toString();
            cursorPosition += clipboardText.length();
        } else {
            builder.replace(start, end, clipboardText);
            value = builder.toString();
            cursorPosition = start + clipboardText.length();
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

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public InputMode getInputMode() {
        return inputMode;
    }

    public enum InputMode {
        DIGITS,
        CHARACTERS,
        CHARACTERS_AND_WHITESPACE,
        DIGITS_AND_CHARACTERS,
        DIGITS_AND_CHARACTERS_AND_UNDERSCORE,
        DIGITS_AND_CHARACTERS_AND_WHITESPACE,
        ALL
    }

}

