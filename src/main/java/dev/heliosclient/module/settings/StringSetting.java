package dev.heliosclient.module.settings;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.InputBox;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

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
        inputBox = new InputBox(180, 13, value,characterLimit,inputMode);
        inputBoxCompact = new InputBox(widthCompact - 4, 12, value,characterLimit,inputMode);
    }


    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),name, x + 2, y + 5,defaultColor,10f);
        inputBox.render(drawContext, x, y + 15, mouseX, mouseY, textRenderer);
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        FontManager.fxfontRenderer.drawString(drawContext.getMatrices(),name, x + 2, y + 5,defaultColor,8f);
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



