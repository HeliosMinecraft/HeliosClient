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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.heliosclient.util.color.ColorUtils.reset;

public class MultiLineInputBoxOld implements Listener {
    protected final InputBox.InputMode inputMode;
    public int x, y, width, height;
    public boolean displayLineNos = true;
    public boolean doSyntaxHighLighting = true;
    public boolean autoScroll = false;
    protected List<String> lines;
    protected boolean focused = false;
    protected int cursorPosition = 0;
    protected int cursorLine = 0;
    protected int selectionStart = 0;
    protected int selectionEnd = 0;
    protected int scrollOffset = 0, hScrollOffset;
    protected long characterLimit;
    protected boolean selecting = false;
    protected boolean selectedAll = false;
    protected Screen screen;
    List<String> declaredVariables = new ArrayList<>();

    public MultiLineInputBoxOld(int width, int height, String value, long characterLimit, InputBox.InputMode inputMode) {
        this.width = width;
        this.height = height;
        //this.value = value;
        setText(value);
        this.characterLimit = characterLimit;
        this.lines = new CopyOnWriteArrayList<>();
        this.inputMode = inputMode;
        EventManager.register(this);
    }

    @SubscribeEvent
    public void keyHeld(KeyHeldEvent event) {
        int keyCode = event.getKey();
        if (focused && canWrite()) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (cursorPosition == 0 && cursorLine > 0 && !selecting) {
                        // If the cursor is at the start of a line (other than the first line),
                        // remove the newline character at the end of the previous line and append the current line to it
                        String previousLine = lines.get(cursorLine - 1);
                        String currentLine = getCurrentLine();
                        lines.set(cursorLine - 1, previousLine + currentLine);
                        lines.remove(cursorLine);

                        // Move the cursor to the end of the previous line
                        cursorLine--;
                        cursorPosition = previousLine.length();
                    } else if (!lines.isEmpty() && cursorPosition >= 0) {
                        String currentLine = getCurrentLine();
                        if (selecting) {
                            if (cursorPosition <= currentLine.length()) {
                                currentLine = currentLine.substring(0, selectionStart) + currentLine.substring(selectionEnd);
                                lines.set(cursorLine, currentLine);
                                cursorPosition = selectionStart;
                                selecting = false;
                                selectionStart = 0;
                                selectionEnd = 0;
                            }
                        } else if (cursorPosition <= currentLine.length()) {
                            currentLine = currentLine.substring(0, cursorPosition - 1) + currentLine.substring(cursorPosition);
                            lines.set(cursorLine, currentLine);
                            cursorPosition--;
                        }
                    }
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    if (selecting) {
                        String currentLine = getCurrentLine();
                        if (cursorPosition <= currentLine.length()) {
                            currentLine = currentLine.substring(0, selectionStart) + currentLine.substring(selectionEnd);
                            lines.set(cursorLine, currentLine);
                            cursorPosition = selectionStart;
                            selecting = false;
                            selectionStart = 0;
                            selectionEnd = 0;
                        }
                    } else if (cursorPosition != lines.get(cursorLine).length()) {
                        String currentLine = getCurrentLine();
                        currentLine = currentLine.substring(0, cursorPosition) + currentLine.substring(cursorPosition + 1);
                        lines.set(cursorLine, currentLine);
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
                case GLFW.GLFW_KEY_UP -> {
                    cursorLine = Math.max(0, cursorLine - 1);
                    selectionStart = 0;
                    selectionEnd = 0;
                }
                case GLFW.GLFW_KEY_DOWN -> {
                    cursorLine = Math.min(lines.size() - 1, cursorLine + 1);
                    selectionStart = 0;
                    selectionEnd = 0;
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
        this.cursorLine = 0;
        this.cursorPosition = 0;
        this.scrollOffset = 0;
        this.lines.clear();
    }

    @SubscribeEvent
    public void mouseClicked(MouseClickEvent event) {
        if (screen != null && event.getScreen() == screen) {
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            isFocusedHover(mouseX, mouseY);
            if ((mouseX >= x + 1 && mouseX <= x + 3 + width && mouseY >= y && mouseY <= (y + height)) && isFocused()) {
                float textHeight = Renderer2D.getCustomStringHeight(FontRenderers.Small_fxfontRenderer);

                int relativeX = (int) Math.round(mouseX - this.x - (displayLineNos ? 20 : 5)); // Adjust for the 20 pixel offset
                int relativeY = (int) (mouseY - this.y + 5 + scrollOffset * textHeight);

                // Calculate the cursor line as before
                this.cursorLine = MathHelper.floor(relativeY / textHeight);
                this.cursorLine = Math.min(this.lines.size() - 1, this.cursorLine);

                // Get the text of the current line
                String lineText = getCurrentLine();

                // Calculate the cursor position based on the width of the text
                this.cursorPosition = 0;
                int textWidth = 0;
                while (textWidth <= relativeX && this.cursorPosition < lineText.length()) {
                    textWidth += (int) FontRenderers.Small_fxfontRenderer.getStringWidth(String.valueOf(getCharAtCursor()));
                    ++this.cursorPosition;
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

    //Todo: Buggy asf
    public String addLuaSyntaxHighlighting(String line) {
        // If the line is a comment, color it green (doc comment) or gray (regular comment)
        if (line.trim().startsWith("--")) {
            String color = line.trim().startsWith("---") ? ColorUtils.green : ColorUtils.gray;
            return color + line;
        }

        // Define regex patterns for different Lua tokens
        Pattern keywordPattern = Pattern.compile("\\b(and|break|do|else|elseif|end|for|function|if|in|local|nil|not|or|repeat|return|then|until|while)\\b");
        Pattern numberPattern = Pattern.compile("\\b\\d+\\b");
        Pattern stringPattern = Pattern.compile("\".*?\"|'.*?'");
        Pattern commentPattern = Pattern.compile("--.*?\\n");
        Pattern booleanPattern = Pattern.compile("\\b(true|false)\\b");
        Pattern methodPattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*\\.[a-zA-Z_][a-zA-Z0-9_]*\\()([^)]*)(\\)|\\b[a-zA-Z_][a-zA-Z0-9_]*\\:\\b[a-zA-Z_][a-zA-Z0-9_]*\\()([^)]*)(\\))");
        Pattern functionPattern = Pattern.compile("\\bfunction\\s+[a-zA-Z_][a-zA-Z0-9_]*\\(");
        Pattern variableDeclarationPattern = Pattern.compile("\\blocal\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\b");

        // Store strings and replace them with placeholders
        List<String> strings = new ArrayList<>();
        Matcher stringMatcher = stringPattern.matcher(line);
        while (stringMatcher.find()) {
            strings.add(stringMatcher.group());
        }
        line = stringMatcher.replaceAll("\"__STRING__\"");

        // Add declared variables to the set
        Matcher variableDeclarationMatcher = variableDeclarationPattern.matcher(line);
        while (variableDeclarationMatcher.find()) {
            String variable = variableDeclarationMatcher.group(1);
            declaredVariables.add(variable);
        }

        // Replace each token with its colored version
        line = keywordPattern.matcher(line).replaceAll(ColorUtils.red + "$0" + reset);
        line = numberPattern.matcher(line).replaceAll(ColorUtils.blue + "$0" + reset);
        line = commentPattern.matcher(line).replaceAll(ColorUtils.gray + "$0" + reset);
        line = booleanPattern.matcher(line).replaceAll(ColorUtils.aqua + "$0" + reset);
        line = functionPattern.matcher(line).replaceAll(ColorUtils.darkMagenta + "$0" + reset);
        line = methodPattern.matcher(line).replaceAll(ColorUtils.gold + "$0" + reset);

        // Highlight undeclared variables
        for (String variable : declaredVariables) {
            // Escape the variable name to ensure it's treated as a literal string
            String escapedVariable = Pattern.quote(variable);
            Pattern variablePattern = Pattern.compile("\\b" + escapedVariable + "\\b");
            line = variablePattern.matcher(line).replaceAll(ColorUtils.yellow + variable + reset);
        }

        // Replace placeholders with original strings
        stringMatcher.reset(line);
        int i = 0;
        StringBuilder sb = new StringBuilder();
        while (stringMatcher.find()) {
            stringMatcher.appendReplacement(sb, Matcher.quoteReplacement(ColorUtils.darkGreen + strings.get(i++) + reset));
        }
        stringMatcher.appendTail(sb);
        line = sb.toString();

        return line;
    }

  /*  public String addSyntaxHighlightingOLD(String line) {
        StringBuilder highlightedLine = new StringBuilder();
        String[] words = line.split(" ");

        // Regular expressions for variables and strings
        Pattern variablePattern = Pattern.compile("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b");
        Pattern methodPattern = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*[\\.\\:][a-zA-Z_][a-zA-Z0-9_]*");

        // Check if the complete line is a comment or a doc
        if (line.trim().startsWith("--")) {
            String color = line.trim().startsWith("---") ? ColorUtils.green : ColorUtils.gray;
            return color + line;
        }

        boolean isFunctionDeclaration = false;
        boolean isParameterList = false;
        boolean isComment = false;
        boolean isDoc = false;
        boolean isString = false;
        boolean isLocalVariable = false;
        boolean isMethodCall = false;

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String color;

            // Check if the word is a comment or a doc
            if (word.startsWith("--")) {
                isComment = true;
                isDoc = word.startsWith("---");
            }

            if (isComment) {
                color = isDoc ? ColorUtils.green : ColorUtils.gray;
                highlightedLine.append(color).append(word).append(" ");
                continue;
            }

            // Check if the word is a declared variable
            if (declaredVariables.contains(word) && !isString) {
                color = ColorUtils.magenta; // Purple for variable usage
                highlightedLine.append(color).append(word).append(" ");
                continue;
            }
            // Check if the word is a method call
            if (methodPattern.matcher(word).matches() || (isMethodCall && !word.equals(")"))) {
                if (word.contains(".") || word.contains(":")) {
                    String[] parts = word.split("[\\.\\:]");
                    color = ColorUtils.magenta; // Magenta for variables
                    highlightedLine.append(color).append(parts[0]).append(".");
                    color = ColorUtils.gold; // Gold for method calls
                    highlightedLine.append(color).append(parts[1]).append(" ");
                    isMethodCall = true;
                    continue;
                } else if (word.equals(")")) {
                    color = ColorUtils.gold; // Gold for method calls
                    highlightedLine.append(color).append(word).append(" ");
                    isMethodCall = false;
                    continue;
                }
            }

            // Check if the word is a local variable declaration
            if (word.startsWith("local")) {
                isLocalVariable = true;
                color = ColorUtils.red; // Red for the 'local' keyword
                highlightedLine.append(color).append(word).append(" ");
            }

            // Check if the next word is a local variable name
            if (isLocalVariable && i + 1 < words.length) {
                String nextWord = words[i + 1];
                if (!isKeyword(nextWord) && !isBoolean(nextWord) && !isNil(nextWord) && !isNumber(nextWord) && !isString) {
                    color = ColorUtils.magenta; // Magenta for local variables
                    highlightedLine.append(color).append(nextWord).append(" ");
                    declaredVariables.add(nextWord);
                    i++; // Skip the next word in the next iteration
                }
                isLocalVariable = false;
                continue;
            }
            // Check if the word is a string
            if (word.startsWith("\"") || word.startsWith("'") || isString) {
                isString = !word.endsWith("\"") && !word.endsWith("'");
                color = ColorUtils.darkGreen; // Dark green for strings
                highlightedLine.append(color).append(word).append(" ");
                continue;
            }

            // Check if the word is a function declaration
            if (word.equals("function")) {
                color = ColorUtils.red; // Red for keywords
                highlightedLine.append(color).append(word).append(" ");
                isFunctionDeclaration = true;
                continue;
            }

            // Check if the next word is a function name
            if (isFunctionDeclaration && !word.startsWith("(")) {
                color = ColorUtils.yellow; // Yellow for function names
                highlightedLine.append(color).append(word).append(" ");
            }
            // Check if the word is a method parameter
             if ((isFunctionDeclaration || isMethodCall) && (word.startsWith("(") || isParameterList)) {
                isParameterList = true;
                color = ColorUtils.darkGreen; // Dark green for method parameters
                highlightedLine.append(color).append(word).append(" ");
                if (word.endsWith(")")) {
                    isParameterList = false;
                    isFunctionDeclaration = false;
                    isMethodCall = false;
                }
            }
            // Check if the word is a boolean value
            else if (isBoolean(word)) {
                color = ColorUtils.aqua; // Aqua for boolean values
                highlightedLine.append(color).append(word).append(" ");
            }
            // Check if the word is a keyword
            else if (isKeyword(word)) {
                color = ColorUtils.red; // Red for keywords
                highlightedLine.append(color).append(word).append(" ");
            }
            // Check if the word is nil
            else if (isNil(word)) {
                color = ColorUtils.darkRed; // Dark red for nil
                highlightedLine.append(color).append(word).append(" ");
            }
            // Check if the word is a number
            else if (isNumber(word)) {
                color = ColorUtils.blue; // Blue for numbers
                highlightedLine.append(color).append(word).append(" ");
            }
            // Check if the word is a variable
            else if (variablePattern.matcher(word).matches()) {
                color = ColorUtils.magenta; // Magenta for variables
                highlightedLine.append(color).append(word).append(" ");
            }
            // For all other words
            else {
                color = ColorUtils.white; // White for everything else
                highlightedLine.append(color).append(word).append(" ");
            }
        }

        return highlightedLine.toString();
    }

   */

    private boolean isKeyword(String word) {
        return Arrays.asList("and", "break", "do", "else", "elseif", "end", "false", "for", "function", "if", "in", "local", "not", "or", "repeat", "return", "then", "true", "until", "while").contains(word);
    }

    private boolean isNil(String word) {
        return word.contains("nil");
    }

    private boolean isNumber(String word) {
        try {
            Double.parseDouble(word);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isBoolean(String word) {
        return word.contains("true") || word.contains("false");
    }


    public char getCharAtCursor() {
        String currentLine = getCurrentLine();
        if (cursorPosition >= 0 && cursorPosition < currentLine.length()) {
            return currentLine.charAt(cursorPosition);
        } else {
            // Return a special value to indicate that the cursor is not currently over a character
            return '\0';
        }
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
                    line = addLuaSyntaxHighlighting(line);
                }

                FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), line, x + xOffset, textY, 0xFFFFFFFF);
                textY += Renderer2D.getCustomStringHeight(FontRenderers.Small_fxfontRenderer);
            }
            declaredVariables.clear();
        }
        displayCursor(drawContext, textHeight, xOffset, 0.5f);
    }

    public void displayCursor(DrawContext drawContext, float textHeight, float xOffset, float cursorWidth) {
        // Draw the cursor
        if (!lines.isEmpty() && focused) {
            // Get the current line of text
            String currentLine = getCurrentLine();

            // Calculate the width of the text before the cursor
            String textBeforeCursor = currentLine.substring(0, cursorPosition);
            float textWidthBeforeCursor = FontRenderers.Small_fxfontRenderer.getStringWidth(textBeforeCursor);

            // Calculate the x and y coordinates of the cursor
            float cursorX = x + textWidthBeforeCursor + xOffset;
            float cursorY = y + 5 + cursorLine * textHeight - scrollOffset * textHeight;

            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(),
                    cursorX,
                    cursorY,
                    cursorWidth,
                    textHeight - 0.5f,
                    ColorUtils.rgbaToInt(150, 150, 150, 255));
        }
    }


    public void drawSelectionBox(DrawContext drawContext, float textY, float xOffSet, float textHeight) {
        // Draw selection box
        if (focused && selecting && selectionStart != selectionEnd) {
            String strBeforeStart = lines.get(cursorLine).substring(0, Math.min(selectionStart, lines.get(cursorLine).length()));
            String strBeforeEnd = lines.get(cursorLine).substring(0, Math.min(selectionEnd, lines.get(cursorLine).length()));
            float startX = (x + xOffSet + FontRenderers.Small_fxfontRenderer.getStringWidth(strBeforeStart));
            float endX = (x + xOffSet + FontRenderers.Small_fxfontRenderer.getStringWidth(strBeforeEnd));
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), startX, textY, endX - startX, textHeight, new Color(0, 166, 255, 64).getRGB());
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

        cursorPosition = MathHelper.clamp(cursorPosition, 0, getCurrentLine().length());
        //cursorPosition = Math.max(0, Math.min(cursorPosition, lines.get(cursorLine).length()));

        displaySegment(drawContext, (float) y + 5, displayLineNos ? lineNosWidth + 8 : 5, textHeight);

        drawSelectionBox(drawContext, (float) y + 5 + (textHeight * (cursorLine - scrollOffset)), displayLineNos ? 20 : 5, textHeight);

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
        if (lines.isEmpty() || cursorLine < 0 || cursorLine > lines.size()) {
            return "";
        }

        return lines.get(cursorLine);
    }

    @SubscribeEvent
    public void keyPressed(KeyPressedEvent event) {
        int keyCode = event.getKey();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            focused = false;
            return;
        }

        if (focused && canWrite()) {
            if (Screen.isSelectAll(keyCode)) {
                selecting = true;
                selectedAll = true;
                selectionStart = 0;
                selectionEnd = lines.get(cursorLine).length();
            }
            if (selectedAll && (GLFW.GLFW_KEY_DELETE == keyCode || GLFW.GLFW_KEY_BACKSPACE == keyCode)) {
                lines.set(cursorLine, "");
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
                lines.set(cursorLine, "");
            }

            switch (keyCode) {
                case GLFW.GLFW_KEY_ENTER,
                     GLFW.GLFW_KEY_KP_ENTER -> {
                    // Insert a newline character at the cursor position
                    StringBuilder builder = new StringBuilder(lines.get(cursorLine));
                    if (builder.length() >= cursorPosition) {
                        builder.insert(cursorPosition, '\n');
                    } else if (builder.isEmpty()) {
                        builder.insert(0, '\n');
                    }
                    lines.set(cursorLine, builder.toString());

                    // Split the current line into two lines
                    String[] splitLines = lines.get(cursorLine).split("\n", 2);
                    lines.set(cursorLine, splitLines[0]);
                    lines.add(cursorLine + 1, splitLines[1]);

                    // Move the cursor to the start of the new line
                    cursorLine++;
                    cursorPosition = 0;
                }
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
                    selectionEnd = lines.get(cursorLine).length();
                } else {
                    selecting = false;
                }
                cursorPosition = lines.get(cursorLine).length();
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
            } else {
                String errorMessage = switch (inputMode) {
                    case DIGITS -> "Enter only digits 0-9";
                    case CHARACTERS -> "Enter only letters a-z // A-Z";
                    case CHARACTERS_AND_WHITESPACE -> "Enter only letters or blank a-z // A-Z // ";
                    case DIGITS_AND_CHARACTERS -> "Enter only digits or letters a-z // A-Z // 0-9";
                    case DIGITS_AND_CHARACTERS_AND_UNDERSCORE -> "Enter only digits or letters or underscore a-z // A-Z // 0-9 // _";
                    case DIGITS_AND_CHARACTERS_AND_WHITESPACE -> "Enter only digits or letters or blank a-z // A-Z //   // 0-9";
                    case ALL -> null;
                    case PREDICATE -> "Invalid character";
                };
                if (errorMessage != null) {
                    AnimationUtils.addErrorToast(ColorUtils.red + errorMessage, true, 1000);
                }
            }
        }
    }


    private void insertCharacter(char chr) {
        String currentLine = getCurrentLine();
        if (currentLine.length() <= characterLimit) {
            // Check if text is selected
            if (selecting) {
                // Replace the selected text with the new character
                int start = Math.min(selectionStart, selectionEnd);
                int end = Math.max(selectionStart, selectionEnd);
                StringBuilder builder = new StringBuilder(currentLine);
                builder.replace(start, end, String.valueOf(chr));
                lines.set(cursorLine, builder.toString());
                cursorPosition = start + 1;

                // Clear the selection
                selecting = false;
                selectionStart = selectionEnd = cursorPosition;
            } else if (cursorPosition >= 0 && cursorPosition <= currentLine.length()) {
                // Insert the new character at the cursor position
                currentLine = currentLine.substring(0, cursorPosition) + chr + currentLine.substring(cursorPosition);
                lines.set(cursorLine, currentLine);
                cursorPosition++;
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


    public void moveCursor(int offset) {
        this.setCursorPos(this.cursorPosition + offset);
    }

    public void setCursorPos(int pos) {
        this.cursorPosition = pos;
    }

    public boolean canWrite() {
        return true; // You can modify this method to add conditions for when the user can write to the input box
    }

    public void paste() {
        // Get the text from the system clipboard
        String clipboardText = MinecraftClient.getInstance().keyboard.getClipboard();

        // If text is selected, replace it with the clipboard text
        // Otherwise, insert the clipboard text at the cursor position
        String currentLine = getCurrentLine();
        StringBuilder builder = new StringBuilder(currentLine);
        if (selectionStart == selectionEnd) {
            builder.insert(cursorPosition, clipboardText);
            cursorPosition += clipboardText.length();
        } else {
            int start = Math.min(selectionStart, selectionEnd);
            int end = Math.max(selectionStart, selectionEnd);
            builder.replace(start, end, clipboardText);
            cursorPosition = start + clipboardText.length();
        }
        currentLine = builder.toString();

        // Split the current line into multiple lines if the clipboard text contains newline characters
        String[] splitLines = currentLine.split("\n");
        lines.set(cursorLine, splitLines[0]);
        for (int i = 1; i < splitLines.length; i++) {
            lines.add(cursorLine + i, splitLines[i]);
        }

        // Move the cursor to the correct line and column
        cursorLine += splitLines.length - 1;
        cursorPosition = splitLines[splitLines.length - 1].length();

        // Clear the selection
        selectionStart = selectionEnd = cursorPosition;
    }


    public String getTextToCopy() {
        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);
        return lines.get(cursorLine).substring(start, end);
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

    public List<String> getlines() {
        return lines;
    }

    public void setlines(List<String> lines) {
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
}

