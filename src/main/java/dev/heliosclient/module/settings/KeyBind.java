package dev.heliosclient.module.settings;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.KeycodeToString;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;

public class KeyBind extends Setting {

    public int value;
    public boolean listening = false;
    public static boolean listeningKey = false;

    Module_ module;

    public KeyBind(String name, String description, Module_ module, Integer value, BooleanSupplier shouldRender) {
        super(shouldRender);
        this.module = module;
        this.name = name;
        this.description = description;
        this.value = value;
        this.height = 24;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        if (listening) {
            Renderer2D.drawFixedString(drawContext.getMatrices(), name + ": §lLISTENING", x + 2, y + 8, defaultColor);
        } else if (value == 0) {
            Renderer2D.drawFixedString(drawContext.getMatrices(), name + ": None", x + 2, y + 8, defaultColor);
        } else {
            String keyName = KeycodeToString.translate(value);
            Renderer2D.drawFixedString(drawContext.getMatrices(), name + ": " + keyName, x + 2, y + 8, defaultColor);
        }

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
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        if (listening) {
            compactFont.drawString(drawContext.getMatrices(), name + ": §lLISTENING", x + 2, y + 6, defaultColor);
        } else if (value == 0) {
            compactFont.drawString(drawContext.getMatrices(), name + ": None", x + 2, y + 6, defaultColor);
        } else {
            String keyName = KeycodeToString.translate(value);
            compactFont.drawString(drawContext.getMatrices(), name + ": " + keyName, x + 2, y + 6, defaultColor);
        }

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
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.value = 0;
            } else {
                this.value = keyCode;
            }
            listening = false;
            listeningKey = false;
            module.onSettingChange(this);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered((int) mouseX, (int) mouseY) && button == 0) {
            listening = !listening;
            listeningKey = !listeningKey;
        }
    }

    public static class Builder extends SettingBuilder<Builder, Integer, KeyBind> {
        Module_ module;

        public Builder() {
            super(0);
        }

        public Builder module(Module_ module) {
            this.module = module;
            return this;
        }

        @Override
        public KeyBind build() {
            return new KeyBind(name, description, module, value, shouldRender);
        }
    }
}
