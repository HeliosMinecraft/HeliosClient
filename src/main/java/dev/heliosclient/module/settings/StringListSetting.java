package dev.heliosclient.module.settings;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class StringListSetting extends Setting {
    private final List<InputBox> inputBox = new ArrayList<>();
    private final int characterLimit;
    public String[] values;
    String description;
    private final InputBox.InputMode inputMode;

    public StringListSetting(String name, String description, String[] values, int defaultBoxes, int characterLimit, InputBox.InputMode inputMode, BooleanSupplier shouldRender) {
        super(shouldRender);
        this.name = name;
        this.values = values;
        this.description = description;
        this.height = 26 + defaultBoxes * 15;
        this.characterLimit = characterLimit;
        this.inputMode = inputMode;
        for (int i = 0; i < defaultBoxes; i++) {
            inputBox.add(new InputBox(160, 12, values[i], characterLimit, inputMode));
        }
    }


    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        this.height = 26 + inputBox.size() * 16;
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();
        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 5, defaultColor);

        // Draw a '+' button next to the text
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 165, y + 5, 11, 11, Color.black.getRGB());
        Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x + 165, y + 5, 11, 11, 0.4f, (hoveredOverAdd(mouseX, mouseY)) ? Color.WHITE.getRGB() : Color.GRAY.getRGB());
        drawContext.drawHorizontalLine(x + 167, x + 173, y + 10, Color.GREEN.getRGB());
        drawContext.drawVerticalLine(x + 170, y + 6, y + 14, Color.GREEN.getRGB());
        int boxOffset = y + 20;
        for (InputBox box : inputBox) {
            box.render(drawContext, x, boxOffset, mouseX, mouseY, textRenderer);

            // Draw a '-' button next to the text
            Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 165, boxOffset, 11, 11, Color.black.getRGB());
            drawContext.drawHorizontalLine(x + 168, x + 172, boxOffset + 5, Color.RED.getRGB());
            Renderer2D.drawOutlineBox(drawContext.getMatrices().peek().getPositionMatrix(), x + 165, boxOffset, 11, 11, 0.4f, (hoveredOverRemove(mouseX, mouseY, boxOffset)) ? Color.WHITE.getRGB() : Color.GRAY.getRGB());
            boxOffset += 16;
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        int boxOffset = y;
        // Use a regular for loop with an index variable
        if (hoveredOverAdd(mouseX, mouseY)) {
            inputBox.add(new InputBox(160, 13, "", characterLimit, inputMode)); // Add a new empty box to the list
        }
        for (int i = 0; i < inputBox.size(); i++) {
            InputBox box = inputBox.get(i); // Get the box at the current index
            if (hoveredOverRemove(mouseX, mouseY, boxOffset)) {
                inputBox.remove(i); // Remove the box at the current index
                i--; // Decrement the index to account for the removal
            } else {
                box.mouseClicked(mouseX, mouseY, button);
            }
            boxOffset += 16;

        }
    }


    public int getCharacterLimit() {
        return characterLimit;
    }

    public String[] getValue() {
        return values;
    }

    public List<InputBox> getInputBox() {
        return inputBox;
    }

    public boolean hoveredOverRemove(double mouseX, double mouseY, int boxOffset) {
        return mouseX >= x + 165 && mouseX <= x + 176 && mouseY >= boxOffset && mouseY <= boxOffset + 11;
    }

    public boolean hoveredOverAdd(double mouseX, double mouseY) {
        return mouseX >= x + 165 && mouseX <= x + 176 && mouseY >= y + 5 && mouseY <= y + 16;
    }

    public String getDescription() {
        return description;
    }

    public static class Builder extends SettingBuilder<Builder, String[], StringListSetting> {
        int defaultBoxes, characterLimit;
        InputBox.InputMode inputMode;

        public Builder() {
            super(new String[]{});
        }

        public Builder defaultBoxes(int defaultBoxes) {
            this.defaultBoxes = defaultBoxes;
            return this;
        }

        public Builder characterLimit(int characterLimit) {
            this.characterLimit = characterLimit;
            return this;
        }

        public Builder inputMode(InputBox.InputMode inputMode) {
            this.inputMode = inputMode;
            return this;
        }

        @Override
        public StringListSetting build() {
            return new StringListSetting(name, description, value, defaultBoxes, characterLimit, inputMode, shouldRender);
        }
    }
}



