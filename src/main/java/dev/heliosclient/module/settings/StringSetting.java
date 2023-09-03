package dev.heliosclient.module.settings;

import dev.heliosclient.system.ColorManager;
import dev.heliosclient.util.InputBox;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class StringSetting extends Setting {
    private final InputBox inputBox;
    private final int characterLimit;
    public String value;
    String description;

    public StringSetting(String name, String description, String value, int characterLimit) {
        this.name = name;
        this.value = value;
        this.description = description;
        this.height = 38;
        this.characterLimit = characterLimit;
        inputBox = new InputBox(180, 13, value, characterLimit);
    }


    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 5, ColorManager.INSTANCE.defaultTextColor(), false);
        inputBox.render(drawContext, x, y, mouseX, mouseY, textRenderer);
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
    }

    @Override
    public void keyReleased(int keyCode, int scanCode, int modifiers) {
        inputBox.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        inputBox.keyPressed(keyCode, scanCode, modifiers);
    }

    public void charTyped(char chr, int modifiers) {
        inputBox.charTyped(chr, modifiers);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        inputBox.setFocused(hovered((int) mouseX, (int) mouseY));
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



