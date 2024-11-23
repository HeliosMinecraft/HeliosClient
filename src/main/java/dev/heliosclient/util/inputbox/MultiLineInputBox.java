package dev.heliosclient.util.inputbox;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.input.CharTypedEvent;
import dev.heliosclient.event.events.input.KeyHeldEvent;
import dev.heliosclient.event.events.input.KeyPressedEvent;
import dev.heliosclient.event.events.input.MouseClickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.inputbox.lua.LuaSyntaxManager;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public class MultiLineInputBox implements Listener {
    protected final InputBox.InputMode inputMode;
    public int x, y, width, height;
    public boolean displayLineNos = true;

    public boolean doSyntaxHighLighting = true;
    public boolean doAutoComplete = false;
    public boolean autoScroll = false;

    protected List<String> lines;
    protected boolean focused = false;
    //Our cursor pos, with each x int meaning 1 character and each y int meaning 1 line
    protected Point cursorPos = new Point();

    //Same as cursor pos but for handling multi-line selection
    protected Point selectionStart = new Point(0, 0);
    protected Point selectionEnd = new Point(0, 0);

    //Scroll offset. TODO: Horizontal scroll
    protected int scrollOffset = 0, hScrollOffset;

    //Max characters that should be entered. For multi-line, its probably a lot.
    protected long characterLimit;

    //Booleans for rendering and selection
    protected boolean selecting = false;
    protected boolean selectedAll = false;
    protected Screen screen;

    //Basic SyntaxManager with the auto correct
    protected LuaSyntaxManager syntaxMan = new LuaSyntaxManager();


    //Autocomplete
    private int selectedSuggestionIndex;
    private List<String> autoCompleteSuggestions;
    private boolean showAutoComplete = false;


    public MultiLineInputBox(int width, int height, String value, long characterLimit, InputBox.InputMode inputMode) {
        this.width = width;
        this.height = height;
        //this.value = value;
        setText(value);
        cursorPos.setLocation(0, 0);
        this.characterLimit = characterLimit;
        this.lines = new CopyOnWriteArrayList<>();
        this.inputMode = inputMode;
        this.selectedSuggestionIndex = -1;
        this.autoCompleteSuggestions = new ArrayList<>();
        EventManager.register(this);
    }

    @SubscribeEvent
    public void keyHeld(KeyHeldEvent event) {
        int keyCode = event.getKey();
        boolean shiftPressed = GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

        if (focused && canWrite()) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    dontShowAutoCompleteSuggestions();
                    if (selecting) {
                        deleteSelection();
                    } else if (cursorPos.x > 0) {
                        String currentLine = getCurrentLine();
                        StringBuilder builder = new StringBuilder(currentLine);
                        builder.deleteCharAt(cursorPos.x - 1);
                        lines.set(cursorPos.y, builder.toString());
                        cursorPos.x--;
                    } else if (cursorPos.y > 0) {
                        cursorPos.y--;
                        cursorPos.x = lines.get(cursorPos.y).length();
                        lines.set(cursorPos.y, lines.get(cursorPos.y) + lines.remove(cursorPos.y + 1));
                    }
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    dontShowAutoCompleteSuggestions();
                    if (selecting) {
                        deleteSelection();
                    } else {
                        String currentLine = getCurrentLine();
                        if (cursorPos.x < currentLine.length()) {
                            StringBuilder builder = new StringBuilder(currentLine);
                            builder.deleteCharAt(cursorPos.x);
                            lines.set(cursorPos.y, builder.toString());
                        } else if (cursorPos.y < lines.size() - 1) {
                            lines.set(cursorPos.y, currentLine + lines.remove(cursorPos.y + 1));
                        }
                    }
                }
                case GLFW.GLFW_KEY_LEFT -> {
                    dontShowAutoCompleteSuggestions();
                    if (shiftPressed) {
                        if (!selecting) {
                            selectionStart = new Point(cursorPos.x, cursorPos.y);
                            selecting = true;
                        }
                        cursorPos.x = Math.max(0, cursorPos.x - 1);
                        selectionEnd = new Point(cursorPos.x, cursorPos.y);
                    } else {
                        cursorPos.x = Math.max(0, cursorPos.x - 1);
                        selecting = false;
                    }
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    dontShowAutoCompleteSuggestions();
                    String currentLine = getCurrentLine();
                    if (shiftPressed) {
                        if (!selecting) {
                            selectionStart = new Point(cursorPos.x, cursorPos.y);
                            selecting = true;
                        }
                        cursorPos.x = Math.min(currentLine.length(), cursorPos.x + 1);
                        selectionEnd = new Point(cursorPos.x, cursorPos.y);
                    } else {
                        cursorPos.x = Math.min(currentLine.length(), cursorPos.x + 1);
                        selecting = false;
                    }
                }
                case GLFW.GLFW_KEY_UP -> {
                    if (showAutoComplete) {
                        selectedSuggestionIndex = Math.max(0, selectedSuggestionIndex - 1);
                        return;
                    }
                    dontShowAutoCompleteSuggestions();
                    if (shiftPressed) {
                        if (!selecting) {
                            selectionStart = new Point(cursorPos.x, cursorPos.y);
                            selecting = true;
                        }
                        cursorPos.y = Math.max(0, cursorPos.y - 1);
                        cursorPos.x = Math.min(cursorPos.x, lines.get(cursorPos.y).length());
                        selectionEnd = new Point(cursorPos.x, cursorPos.y);
                    } else {
                        cursorPos.y = Math.max(0, cursorPos.y - 1);
                        cursorPos.x = Math.min(cursorPos.x, lines.get(cursorPos.y).length());
                        selecting = false;
                    }
                    adjustScrollOffset();
                }
                case GLFW.GLFW_KEY_DOWN -> {
                    if (showAutoComplete) {
                        selectedSuggestionIndex = Math.min(autoCompleteSuggestions.size() - 1, selectedSuggestionIndex + 1);
                        return;
                    }
                    dontShowAutoCompleteSuggestions();
                    if (shiftPressed) {
                        if (!selecting) {
                            selectionStart = new Point(cursorPos.x, cursorPos.y);
                            selecting = true;
                        }
                        cursorPos.y = Math.min(lines.size() - 1, cursorPos.y + 1);
                        cursorPos.x = Math.min(cursorPos.x, lines.get(cursorPos.y).length());
                        selectionEnd = new Point(cursorPos.x, cursorPos.y);
                    } else {
                        cursorPos.y = Math.min(lines.size() - 1, cursorPos.y + 1);
                        cursorPos.x = Math.min(cursorPos.x, lines.get(cursorPos.y).length());
                        selecting = false;
                    }
                    adjustScrollOffset();
                }
                case GLFW.GLFW_KEY_HOME -> {
                    dontShowAutoCompleteSuggestions();
                    if (shiftPressed) {
                        if (!selecting) {
                            selectionStart = new Point(cursorPos.x, cursorPos.y);
                            selecting = true;
                        }
                        cursorPos.x = 0;
                        selectionEnd = new Point(cursorPos.x, cursorPos.y);
                    } else {
                        cursorPos.x = 0;
                        selecting = false;
                    }
                }
                case GLFW.GLFW_KEY_END -> {
                    dontShowAutoCompleteSuggestions();
                    if (shiftPressed) {
                        if (!selecting) {
                            selectionStart = new Point(cursorPos.x, cursorPos.y);
                            selecting = true;
                        }
                        cursorPos.x = getCurrentLine().length();
                        selectionEnd = new Point(cursorPos.x, cursorPos.y);
                    } else {
                        cursorPos.x = getCurrentLine().length();
                        selecting = false;
                    }
                }
            }
            if (keyCode == GLFW.GLFW_KEY_TAB) {
                if (showAutoComplete) {
                    selectedSuggestionIndex = Math.min(autoCompleteSuggestions.size() - 1, selectedSuggestionIndex + 1);
                }
            }
        }
    }


    public void setText(String text) {
        this.lines = new CopyOnWriteArrayList<>(Arrays.asList(text.split("\n")));
    }

    public void addLine(String text) {
        this.lines.addAll(Arrays.asList(text.split("\n")));
    }

    public void clearAll() {
        this.focused = false;
        this.cursorPos.setLocation(0, 0);
        this.scrollOffset = 0;
        this.lines.clear();
    }

    @SubscribeEvent
    public void mouseClicked(MouseClickEvent event) {
        if (screen != null && event.getScreen() == screen) {
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();

            if (isFocusedHover(mouseX, mouseY)) {
                int lineHeight = Math.round(Renderer2D.getCustomStringHeight(FontRenderers.Small_fxfontRenderer));
                int relativeX = (int) (mouseX - x - (displayLineNos ? 20 : 5));
                int relativeY = (int) (mouseY - y - 5);

                int clickedLine = MathHelper.clamp((relativeY / lineHeight) + scrollOffset, 0, lines.size() - 1);
                String clickedLineText = lines.get(clickedLine);
                int clickedColumn = 0;

                for (int i = 0; i < clickedLineText.length(); i++) {
                    if (FontRenderers.Small_fxfontRenderer.getStringWidth(clickedLineText.substring(0, i + 1)) > relativeX) {
                        break;
                    }
                    clickedColumn = i + 1;
                }

                if (Screen.hasShiftDown()) {
                    if (!selecting) {
                        selectionStart = new Point(cursorPos.x, cursorPos.y);
                        selecting = true;
                    }
                    selectionEnd = new Point(clickedColumn, clickedLine);
                } else {
                    cursorPos.setLocation(clickedColumn, clickedLine);
                    selecting = false;
                }
            }
        }
    }

    public boolean isFocusedHover(double mouseX, double mouseY) {
        return focused = (mouseX >= x + 1 && mouseX <= x + 3 + width && mouseY >= (y) && mouseY <= (y + height));
    }

    public void update(int x, int y) {
        this.screen = HeliosClient.MC.currentScreen;

        this.x = x;
        this.y = y;
    }


    public void displaySegment(DrawContext drawContext, float y, float xOffset, float textHeight) {
        if (!lines.isEmpty()) {
            //Only display the lines which should be visible. Helps with lag.

            // Calculate the first and last line indices to display
            int firstLineIndex = Math.max(0, scrollOffset);
            int visibleLinesCount = (int) Math.floor(height / textHeight);
            int lastLineIndex = Math.min(lines.size(), firstLineIndex + visibleLinesCount);

            float textY = y - (scrollOffset - firstLineIndex) * textHeight;
            for (int i = firstLineIndex; i < lastLineIndex; i++) {
                String line = lines.get(i);
                if (displayLineNos) {
                    FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), String.valueOf(i), x + 6, textY, Color.WHITE.getRGB());
                }
                // Add syntax highlighting to the line
                if (doSyntaxHighLighting) {
                    line = LuaSyntaxManager.applySyntaxHighlighting(line);
                }

                FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), line, x + xOffset, textY, 0xFFFFFFFF);
                textY += Renderer2D.getCustomStringHeight(FontRenderers.Small_fxfontRenderer);
            }
        }
        displayCursor(drawContext, textHeight, xOffset, 0.5f);
    }

    public void displayCursor(DrawContext drawContext, float textHeight, float xOffset, float cursorWidth) {
        // Draw the cursor
        if (!lines.isEmpty() && focused) {
            // Get the current line of text
            String currentLine = getCurrentLine();

            // Calculate the width of the text before the cursor
            String textBeforeCursor = currentLine.substring(0, cursorPos.x);
            float textWidthBeforeCursor = FontRenderers.Small_fxfontRenderer.getStringWidth(textBeforeCursor);

            // Calculate the x and y coordinates of the cursor
            float cursorX = x + textWidthBeforeCursor + xOffset;
            float cursorY = y + 5 + cursorPos.y * textHeight - scrollOffset * textHeight;

            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(),
                    cursorX,
                    cursorY,
                    cursorWidth,
                    textHeight - 0.5f,
                    ColorUtils.rgbaToInt(150, 150, 150, 255));
        }
    }

    public void renderAutoComplete(DrawContext drawContext, float x, float y, float textHeight) {
        if (showAutoComplete && !autoCompleteSuggestions.isEmpty()) {
            // Calculate the width of the longest suggestion
            float maxWidth = 0;
            for (String suggestion : autoCompleteSuggestions) {
                float width = FontRenderers.Small_fxfontRenderer.getStringWidth(suggestion);
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }

            // Add some padding
            float padding = 4;
            float boxWidth = maxWidth + 2 * padding;
            float boxHeight = autoCompleteSuggestions.size() * textHeight + 2 * padding;

            // Draw the background with rounded corners
            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x, y + textHeight + 2, boxWidth, boxHeight, 5, new Color(0, 0, 0, 150).getRGB());

            // Draw a border around the box
            Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x, y + textHeight + 2, boxWidth, boxHeight, 5, 1, new Color(0, 166, 255).getRGB());

            // Draw each suggestion with some padding
            float suggestionY = y + textHeight + 2 + padding;
            for (int i = 0; i < autoCompleteSuggestions.size(); i++) {
                String suggestion = autoCompleteSuggestions.get(i);
                int color = (i == selectedSuggestionIndex) ? new Color(255, 255, 255).getRGB() : new Color(140, 139, 139).getRGB();
                FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), suggestion, x + padding, suggestionY, color);
                suggestionY += textHeight;
            }
        }
    }

    public void drawSelectionBox(DrawContext drawContext, float textY, float xOffSet, float textHeight) {
        if (focused && selecting && !selectionStart.equals(selectionEnd)) {
            int startLine = Math.min(selectionStart.y, selectionEnd.y);
            int endLine = Math.max(selectionStart.y, selectionEnd.y);

            for (int line = startLine; line <= endLine; line++) {
                String currentLine = lines.get(line);
                float lineStartX = x + xOffSet;
                float lineEndX = x + xOffSet + FontRenderers.Small_fxfontRenderer.getStringWidth(currentLine) + 1;

                if (line == startLine) {
                    lineStartX += FontRenderers.Small_fxfontRenderer.getStringWidth(currentLine.substring(0, MathHelper.clamp(selectionStart.x, 0, currentLine.length())));
                }
                if (line == endLine) {
                    lineEndX = x + xOffSet + FontRenderers.Small_fxfontRenderer.getStringWidth(currentLine.substring(0, MathHelper.clamp(selectionEnd.x, 0, currentLine.length())));
                }

                float adjustedTextY = textY + (line - scrollOffset) * textHeight;
                Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), lineStartX, adjustedTextY, lineEndX - lineStartX, textHeight, new Color(0, 166, 255, 64).getRGB());
            }
        }
    }


    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        update(x, y);

        renderBackground(drawContext);

        Renderer2D.enableScissor(x, y, width, height);
        float lineNosWidth = Math.max(15, FontRenderers.Small_fxfontRenderer.getStringWidth(String.valueOf(lines.size())));
        if (displayLineNos) {
            Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 2, y, true, false, true, false, lineNosWidth + 2, height, 3, Color.GRAY.getRGB());
        }
        float textHeight = Renderer2D.getCustomStringHeight(FontRenderers.Small_fxfontRenderer);

        cursorPos.x = MathHelper.clamp(cursorPos.x, 0, getCurrentLine().length());

        displaySegment(drawContext, (float) y + 5, displayLineNos ? lineNosWidth + 8 : 5, textHeight);

        drawSelectionBox(drawContext, (float) y + 5, displayLineNos ? 20 : 5, textHeight);

        renderAutoComplete(drawContext, x + (displayLineNos ? lineNosWidth + 8 : 5) + FontRenderers.Small_fxfontRenderer.getStringWidth(getCurrentLine().substring(0, cursorPos.x)), y + (cursorPos.y - scrollOffset) * textHeight, textHeight);

        Renderer2D.disableScissor();

        if (autoScroll && (lines.size() + 2 - (height / Renderer2D.getCustomStringHeight(FontRenderers.Small_fxfontRenderer))) - scrollOffset <= 2) {
            scrollOffset = (int) (lines.size() + 2 - (height / Renderer2D.getCustomStringHeight(FontRenderers.Small_fxfontRenderer)));
        }
    }

    public void scrollToLast() {
        scrollOffset = (int) (lines.size() + 2 - (height / Renderer2D.getCustomStringHeight(FontRenderers.Small_fxfontRenderer)));
    }

    public void renderBackground(DrawContext drawContext) {
        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x + 1.2f, y - 0.5f, width + 1.5f, height + 1f, 3, 0.5f, focused ? Color.WHITE.getRGB() : Color.DARK_GRAY.getRGB());
        Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 2, y, width, height, 2, Color.BLACK.getRGB());
    }

    public void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    public String getCurrentLine() {
        if (lines.isEmpty() || cursorPos.y < 0 || cursorPos.y >= lines.size()) {
            return "";
        }
        return lines.get(cursorPos.y);
    }


    @SubscribeEvent
    public void keyPressed(KeyPressedEvent event) {
        int keyCode = event.getKey();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            focused = false;
            return;
        }

        boolean shiftPressed = GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

        if (focused && canWrite()) {
            if (Screen.isSelectAll(keyCode)) {
                selecting = true;
                selectedAll = true;
                selectionStart = new Point(0, cursorPos.y);
                selectionEnd = new Point(lines.get(cursorPos.y).length(), cursorPos.y);
            }
            if (selectedAll && (GLFW.GLFW_KEY_DELETE == keyCode || GLFW.GLFW_KEY_BACKSPACE == keyCode)) {
                lines.set(cursorPos.y, "");
                selectedAll = false;
                selecting = false;
            }
            if (Screen.isCopy(keyCode)) {
                selectedAll = false;
                selecting = false;
                MinecraftClient.getInstance().keyboard.setClipboard(this.getTextToCopy());
                selectionStart = new Point(0, cursorPos.y);
                selectionEnd = new Point(0, cursorPos.y);
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
                lines.set(cursorPos.y, "");
            }
            switch (keyCode) {
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                    if (showAutoComplete) {
                        if (selectedSuggestionIndex >= 0 && selectedSuggestionIndex < autoCompleteSuggestions.size()) {
                            String suggestion = autoCompleteSuggestions.get(selectedSuggestionIndex);
                            String currentLine = getCurrentLine();
                            String prefix = currentLine.substring(0, cursorPos.x);
                            String suffix = currentLine.substring(cursorPos.x);
                            // Find the last word in the prefix
                            int lastSpaceIndex = prefix.lastIndexOf(' ');
                            String lastWord = (lastSpaceIndex == -1) ? prefix : prefix.substring(lastSpaceIndex + 1);
                            // Replace the last word with the suggestion
                            String newPrefix = prefix.substring(0, prefix.length() - lastWord.length()) + suggestion;
                            lines.set(cursorPos.y, newPrefix + suffix);
                            cursorPos.x = newPrefix.length();
                            showAutoComplete = false;
                            return;
                        }
                    }
                    String currentLine = getCurrentLine();
                    String newLine = currentLine.substring(cursorPos.x);
                    lines.set(cursorPos.y, currentLine.substring(0, cursorPos.x));
                    lines.add(cursorPos.y + 1, newLine);
                    cursorPos.y++;
                    cursorPos.x = 0;
                    adjustScrollOffset();
                }
                case GLFW.GLFW_KEY_HOME -> {
                    if (shiftPressed) {
                        if (!selecting) {
                            selectionStart = new Point(cursorPos.x, cursorPos.y);
                            selecting = true;
                        }
                        cursorPos.x = 0;
                        selectionEnd = new Point(cursorPos.x, cursorPos.y);
                    } else {
                        cursorPos.x = 0;
                        selecting = false;
                    }
                }
                case GLFW.GLFW_KEY_END -> {
                    if (shiftPressed) {
                        if (!selecting) {
                            selectionStart = new Point(cursorPos.x, cursorPos.y);
                            selecting = true;
                        }
                        cursorPos.x = getCurrentLine().length();
                        selectionEnd = new Point(cursorPos.x, cursorPos.y);
                    } else {
                        cursorPos.x = getCurrentLine().length();
                        selecting = false;
                    }
                }
            }
        }
    }


    @SubscribeEvent
    public void charTyped(CharTypedEvent event) {
        char chr = event.getCharacter();
        if (focused) {
            Predicate<Character> predicate = inputMode.get();
            if (predicate != null && predicate.test(chr)) {
                insertCharacter(chr);

                // Show auto-complete suggestions after a short delay or when Space is pressed
                if (doAutoComplete) {
                    if (chr == ' ') {
                        dontShowAutoCompleteSuggestions();
                    } else {
                        showAutoCompleteSuggestions();
                    }
                }
            } else {
                String errorMessage = switch (inputMode) {
                    case DIGITS -> "Enter only digits 0-9";
                    case CHARACTERS -> "Enter only letters a-z // A-Z";
                    case CHARACTERS_AND_WHITESPACE -> "Enter only letters or blank a-z // A-Z // ";
                    case DIGITS_AND_CHARACTERS -> "Enter only digits or letters a-z // A-Z // 0-9";
                    case DIGITS_AND_CHARACTERS_AND_UNDERSCORE ->
                            "Enter only digits or letters or underscore a-z // A-Z // 0-9 // _";
                    case DIGITS_AND_CHARACTERS_AND_WHITESPACE ->
                            "Enter only digits or letters or blank a-z // A-Z //   // 0-9";
                    case ALL -> null;
                    case PREDICATE -> "Invalid character";
                };
                if (errorMessage != null) {
                    AnimationUtils.addErrorToast(ColorUtils.red + errorMessage, true, 1000);
                }
            }
        }
    }

    private void adjustScrollOffset() {
        float textHeight = Renderer2D.getCustomStringHeight(FontRenderers.Small_fxfontRenderer);
        int visibleLines = (int) Math.round(Math.ceil(height / textHeight));

        if (cursorPos.y < scrollOffset) {
            scrollOffset = cursorPos.y;
        } else if (cursorPos.y >= scrollOffset + visibleLines) {
            scrollOffset = cursorPos.y - visibleLines + 1;
        }
    }

    private void showAutoCompleteSuggestions() {
        String currentLine = getCurrentLine();
        String textBeforePrefix = currentLine.substring(0, cursorPos.x);
        int indexOfLastSpace = textBeforePrefix.lastIndexOf(" ");
        String prefix = textBeforePrefix;

        if (indexOfLastSpace != -1) {
            prefix = currentLine.substring(indexOfLastSpace + 1);
        }
        autoCompleteSuggestions = syntaxMan.getAutoCompleteSuggestions(prefix);
        showAutoComplete = !autoCompleteSuggestions.isEmpty() && doAutoComplete;
        selectedSuggestionIndex = -1;
    }

    private void dontShowAutoCompleteSuggestions() {
        autoCompleteSuggestions.clear();
        showAutoComplete = false;
        selectedSuggestionIndex = -1;
    }


    private void insertCharacter(char chr) {
        String currentLine = getCurrentLine();
        if (currentLine.length() <= characterLimit) {
            if (selecting) {
                int start = Math.min(selectionStart.x, selectionEnd.x);
                int end = Math.max(selectionStart.x, selectionEnd.x);
                StringBuilder builder = new StringBuilder(currentLine);
                builder.replace(start, end, String.valueOf(chr));
                lines.set(cursorPos.y, builder.toString());
                cursorPos.x = start + 1;

                selecting = false;
                selectionStart = new Point(cursorPos.x, cursorPos.y);
                selectionEnd = new Point(cursorPos.x, cursorPos.y);
            } else if (cursorPos.x >= 0 && cursorPos.x <= currentLine.length()) {
                currentLine = currentLine.substring(0, cursorPos.x) + chr + currentLine.substring(cursorPos.x);
                lines.set(cursorPos.y, currentLine);
                cursorPos.x++;
            }
        }
    }


    public void mouseScrolled(double verticalAmount) {
        if (isFocused()) {
            verticalAmount *= (HeliosClient.CLICKGUI.ScrollSpeed.value / 2.0f);
            // Scroll up or down depending on the direction of the scroll and clamp the value so that
            // you cannot scroll beyond the number of lines
            scrollOffset = (int) MathHelper.clamp(scrollOffset - verticalAmount, 0, lines.size() + 2 - (height / Renderer2D.getCustomStringHeight(FontRenderers.Small_fxfontRenderer)));
        }
    }

    public boolean canWrite() {
        return true; // You can modify this method to add conditions for when the user can write to the input box
    }

    public void paste() {
        String clipboardText = MinecraftClient.getInstance().keyboard.getClipboard();
        String currentLine = getCurrentLine();
        StringBuilder builder = new StringBuilder(currentLine);
        if (selectionStart.equals(selectionEnd)) {
            builder.insert(cursorPos.x, clipboardText);
            cursorPos.x += clipboardText.length();
        } else {
            int start = Math.min(selectionStart.x, selectionEnd.x);
            int end = Math.max(selectionStart.x, selectionEnd.x);
            builder.replace(start, end, clipboardText);
            cursorPos.x = start + clipboardText.length();
        }
        currentLine = builder.toString();

        String[] splitLines = currentLine.split("\n");
        lines.set(cursorPos.y, splitLines[0]);
        for (int i = 1; i < splitLines.length; i++) {
            lines.add(cursorPos.y + i, splitLines[i]);
        }

        cursorPos.y += splitLines.length - 1;
        cursorPos.x = splitLines[splitLines.length - 1].length();

        selectionStart = new Point(cursorPos.x, cursorPos.y);
        selectionEnd = new Point(cursorPos.x, cursorPos.y);
    }

    private void deleteSelection() {
        int startLine = Math.min(selectionStart.y, selectionEnd.y);
        int endLine = Math.max(selectionStart.y, selectionEnd.y);
        int startColumn = selectionStart.x;
        int endColumn = selectionEnd.x;

        if (startLine == endLine) {
            String currentLine = lines.get(startLine);
            StringBuilder builder = new StringBuilder(currentLine);
            builder.delete(Math.min(startColumn, endColumn), Math.max(startColumn, endColumn));
            lines.set(startLine, builder.toString());
        } else {
            String firstLine = lines.get(startLine);
            String lastLine = lines.get(endLine);
            StringBuilder builder = new StringBuilder(firstLine);
            builder.delete(startColumn, firstLine.length());
            builder.append(lastLine.substring(endColumn));
            lines.set(startLine, builder.toString());

            if (endLine >= startLine + 1) {
                lines.subList(startLine + 1, endLine + 1).clear();
            }
        }

        cursorPos.x = startColumn;
        cursorPos.y = startLine;
        selecting = false;
        selectionStart = new Point(cursorPos.x, cursorPos.y);
        selectionEnd = new Point(cursorPos.x, cursorPos.y);
    }

    public String getTextToCopy() {
        int start = Math.min(selectionStart.x, selectionEnd.x);
        int end = Math.max(selectionStart.x, selectionEnd.x);
        return lines.get(cursorPos.y).substring(start, end);
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

    public List<String> getLines() {
        return lines;
    }


    public void setLines(List<String> lines) {
        this.lines = lines;
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

    public long getCharacterLimit() {
        return characterLimit;
    }

    public void setCharacterLimit(int characterLimit) {
        this.characterLimit = characterLimit;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public InputBox.InputMode getInputMode() {
        return inputMode;
    }

    public boolean isEmpty() {
        return lines.isEmpty() || lines.size() < 2;
    }

    public boolean shouldAutoScroll() {
        return autoScroll;
    }

    public MultiLineInputBox setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
        return this;
    }

    public boolean shouldDoSyntaxHighLighting() {
        return doSyntaxHighLighting;
    }

    public MultiLineInputBox setSyntaxHighLighting(boolean doSyntaxHighLighting) {
        this.doSyntaxHighLighting = doSyntaxHighLighting;
        return this;
    }

    public boolean shouldDisplayLineNo() {
        return displayLineNos;
    }

    public MultiLineInputBox setDisplayLineNo(boolean displayLineNos) {
        this.displayLineNos = displayLineNos;
        return this;
    }

    public boolean shouldDoAutoComplete() {
        return doAutoComplete;
    }

    public MultiLineInputBox setAutoComplete(boolean doAutoComplete) {
        this.doAutoComplete = doAutoComplete;
        return this;
    }
}
