package dev.heliosclient.module.settings;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.KeycodeToString;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.fontutils.FontRenderers;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;

public class KeyBind extends Setting<Integer> {
    public static boolean listeningKey = false;
    public static boolean listeningMouse = false;
    public int value;
    public boolean listening = false;
    Module_ module;

    public KeyBind(String name, String description, Module_ module, Integer value, BooleanSupplier shouldRender, int defaultValue) {
        super(shouldRender, defaultValue);
        this.module = module;
        this.name = name;
        this.description = description;
        this.value = value;
        this.height = 24;
        EventManager.register(this);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        if (listening) {
            Renderer2D.drawFixedString(drawContext.getMatrices(), name + ": §lLISTENING", x + 2, y + 8, defaultColor);
        } else if (value == -1) {
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
            FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), name + ": §lLISTENING", x + 2, y + 6, defaultColor);
        } else if (value == -1) {
            FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), name + ": None", x + 2, y + 6, defaultColor);
        } else {
            String keyName = KeycodeToString.translate(value);
            FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), name + ": " + keyName, x + 2, y + 6, defaultColor);
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
                this.value = -1;
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
        super.mouseClicked(mouseX, mouseY, button);
        if(listeningMouse && listening){
            value = button;
            module.onSettingChange(this);
            listening = !listening;
            listeningMouse = false;
        }
        if (hovered((int) mouseX, (int) mouseY) && button == 0 && !listeningMouse) {
            listening = !listening;
            listeningKey = !listeningKey;
            listeningMouse = true;
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
    }

    public static class Builder extends SettingBuilder<Builder, Integer, KeyBind> {
        Module_ module;

        public Builder() {
            super(-1);
        }

        public Builder module(Module_ module) {
            this.module = module;
            return this;
        }

        @Override
        public KeyBind build() {
            return new KeyBind(name, description, module, value, shouldRender, defaultValue);
        }
    }
}
