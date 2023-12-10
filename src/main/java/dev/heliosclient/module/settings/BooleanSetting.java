package dev.heliosclient.module.settings;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.animation.AnimationUtils;
import dev.heliosclient.util.animation.EasingType;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Map;
import java.util.function.BooleanSupplier;

public class BooleanSetting extends Setting<Boolean> {
    public boolean value;
    ISettingChange iSettingChange;
    String description;
    AnimationUtils CheckBoxAnimation = new AnimationUtils();

    public BooleanSetting(String name, String description, ISettingChange iSettingChange, boolean value, BooleanSupplier shouldRender, boolean defaultValue) {
        super(shouldRender, defaultValue);
        this.iSettingChange = iSettingChange;
        this.name = name;
        this.description = description;
        this.heightCompact = 14;
        this.value = value;
        CheckBoxAnimation.FADE_SPEED = 0.1f;
        CheckBoxAnimation.startFading(true, EasingType.QUADRATIC_IN_OUT);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor());

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x + 175, y + 7, 10, 10, 2, 0.7f, 0xFFFFFFFF);
        CheckBoxAnimation.drawFadingAndPoppingBox(drawContext, x + 176.7f, y + 8.7f, 6.3f, 6.3f, value ? 0xFF55FFFF : 0xFF222222, true, 2);

        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 150) {
            Tooltip.tooltip.changeText(description);
        }
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);

        FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), name, x + 3, y + 4, ColorManager.INSTANCE.defaultTextColor());

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x + widthCompact - 12, y + 4, 7, 7, 2, 0.6f, 0xFFFFFFFF);

        CheckBoxAnimation.drawFadingAndPoppingBox(drawContext, x + widthCompact - 11.6f, y + 4.4f, 5f, 5f, value ? 0xAA55FFFF : 0xFF222222, true, 1);

        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 100) {
            Tooltip.tooltip.changeText(description);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (hoveredSetting((int) mouseX, (int) mouseY) && hoveredOverReset(mouseX, mouseY)) {
            value = defaultValue;
            iSettingChange.onSettingChange(this);
            CheckBoxAnimation.startFading(value, EasingType.QUADRATIC_OUT);
        }
        if (hovered((int) mouseX, (int) mouseY) && button == 0) {
            this.value = !value;
            iSettingChange.onSettingChange(this);
            CheckBoxAnimation.startFading(value, EasingType.QUADRATIC_OUT);
        }
    }

    @Override
    public Map<String, Object> saveToToml(Map<String, Object> MAP) {
        MAP.put("value",value);
        return MAP;
    }

    @Override
    public void loadFromToml(Map<String, Object> MAP, Toml toml) {
        value = (boolean)((Map<String,Object>) MAP.get(name.replace(" ",""))).get("value");
        System.out.println(name +": " + value);
    }

    public static class Builder extends SettingBuilder<Builder, Boolean, BooleanSetting> {
        ISettingChange ISettingChange;

        public Builder() {
            super(false);
        }

        public Builder onSettingChange(ISettingChange ISettingChange) {
            this.ISettingChange = ISettingChange;
            return this;
        }

        @Override
        public BooleanSetting build() {
            return new BooleanSetting(name, description, ISettingChange, value, shouldRender, defaultValue);
        }
    }
}

