package dev.heliosclient.module.settings;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class StringSetting extends Setting<String> {
    private final InputBox inputBoxCompact;
    private final int characterLimit;
    private final InputBox.InputMode inputMode;
    private final InputBox inputBox;
    public String value;
    public ISettingChange settingChange;
    String description;

    public StringSetting(String name, String description, String value, int characterLimit, ISettingChange settingChange, InputBox.InputMode inputMode, BooleanSupplier shouldRender, String defaultValue) {
        super(shouldRender, defaultValue);
        this.name = name;
        this.value = value;
        this.description = description;
        this.height = 38;
        this.heightCompact = 25;
        this.characterLimit = characterLimit;
        this.inputMode = inputMode;
        this.settingChange = settingChange;
        inputBox = new InputBox(180, 13, value, characterLimit, inputMode);
        inputBoxCompact = new InputBox(widthCompact - 4, 11, value, characterLimit, inputMode);
    }


    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 5, defaultColor);
        inputBox.render(drawContext, x, y + 15, mouseX, mouseY, textRenderer);
        value = inputBox.getValue();
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), name, x + 2, y + 2, defaultColor);
        inputBoxCompact.render(drawContext, x, Math.round(y + FontRenderers.Small_fxfontRenderer.getStringHeight(name) + 3), mouseX, mouseY, textRenderer);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        inputBox.setText(value);
        super.mouseClicked(mouseX, mouseY, button);
        inputBox.setFocused(hovered((int) mouseX, (int) mouseY));
        inputBoxCompact.setFocused(hovered((int) mouseX, (int) mouseY));
    }

    @Override
    public Object saveToFile(List<Object> objectList) {
        return shouldSaveOrLoad ? value : "";
    }

    @Override
    public void loadFromFile(Map<String, Object> MAP) {
        if (!shouldSaveOrLoad) {
            return;
        }
        if (MAP.get(getSaveName()) == null) {
            value = defaultValue;
            return;
        }
        value = (String) MAP.get(getSaveName());
        inputBox.setValue(value);
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

    public static class Builder extends SettingBuilder<Builder, String, StringSetting> {
        int characterLimit;
        InputBox.InputMode inputMode;
        ISettingChange settingChange;

        public Builder() {
            super("");
        }

        public Builder characterLimit(int characterLimit) {
            this.characterLimit = characterLimit;
            return this;
        }

        public Builder inputMode(InputBox.InputMode inputMode) {
            this.inputMode = inputMode;
            return this;
        }

        public Builder onSettingChange(ISettingChange settingChange) {
            this.settingChange = settingChange;
            return this;
        }

        @Override
        public StringSetting build() {
            return new StringSetting(name, description, value, characterLimit, settingChange, inputMode, shouldRender, defaultValue);
        }
    }
}



