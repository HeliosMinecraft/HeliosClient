package dev.heliosclient.module.settings;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class StringSetting extends Setting {
    private InputBox inputBox;
    private InputBox inputBoxCompact;

    private final int characterLimit;
    public String value;
    String description;
    private InputBox.InputMode inputMode;

    public StringSetting(String name, String description, String value, int characterLimit, InputBox.InputMode inputMode) {
        this.name = name;
        this.value = value;
        this.description = description;
        this.height = 38;
        this.heightCompact = 32;
        this.characterLimit = characterLimit;
        this.inputMode = inputMode;
        inputBox = new InputBox(180, 13, value, characterLimit, inputMode);
        inputBoxCompact = new InputBox(widthCompact - 4, 12, value, characterLimit, inputMode);
    }


    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 5, defaultColor);
        inputBox.render(drawContext, x, y + 15, mouseX, mouseY, textRenderer);
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 5, defaultColor);
        inputBoxCompact.render(drawContext, x, y + 16, mouseX, mouseY, textRenderer);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        inputBox.setFocused(hovered((int) mouseX, (int) mouseY));
        inputBoxCompact.setFocused(hovered((int) mouseX, (int) mouseY));
    }

    public int getCharacterLimit() {
        return characterLimit;
    }

    public String getValue() {
        return value;
    }

    public InputBox getInputBox() {
        return inputBox;
    }

    public String getDescription() {
        return description;
    }
}



