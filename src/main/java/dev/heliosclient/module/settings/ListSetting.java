package dev.heliosclient.module.settings;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.ui.clickgui.ListSettingScreen;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;

public class ListSetting extends Setting {
    public ArrayList<String> value;
    public ArrayList<String> options;
    Screen parentScreen;

    public ListSetting(String name, String description, Screen parentScreen, ArrayList<String> options, ArrayList<String> value, BooleanSupplier shouldRender) {
        super(shouldRender);
        this.name = name;
        this.description = description;
        this.options = options;
        this.parentScreen = parentScreen;
        this.value = value;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        Renderer2D.drawFixedString(drawContext.getMatrices(), name, x + 2, y + 8, defaultColor);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered((int) mouseX, (int) mouseY) && button == 0) {
            MinecraftClient.getInstance().setScreen(new ListSettingScreen(this, parentScreen));
        }
    }

    public boolean isOptionEnabled(String option) {
        return value.contains(option);
    }

    public static class Builder extends SettingBuilder<Builder, ArrayList<String>, ListSetting> {
        ArrayList<String> options;
        Screen parentScreen;

        public Builder() {
            super(new ArrayList<>());
        }

        public Builder parentScreen(Screen parentScreen) {
            this.parentScreen = parentScreen;
            return this;
        }

        public Builder options(ArrayList<String> options) {
            this.options = options;
            return this;
        }

        @Override
        public ListSetting build() {
            return new ListSetting(name, description, parentScreen, options, value, shouldRender);
        }
    }
}