package dev.heliosclient.module.settings;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.KeyboardUtils;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class KeyBind extends Setting<Integer> {
    public static boolean listeningKey = false;
    public static boolean listeningMouse = false;
    public static boolean listening = false;
    public int value;
    public boolean isListening = false;

    public KeyBind(String name, String description, ISettingChange iSettingChange, Integer value, BooleanSupplier shouldRender, int defaultValue) {
        super(shouldRender, defaultValue);
        this.iSettingChange = iSettingChange;
        this.name = name;
        this.description = description;
        this.value = value;
        this.height = 24;
        EventManager.register(this);
    }

    public static int none() {
        return -1;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        if (isListening) {
            Renderer2D.drawFixedString(drawContext.getMatrices(), name + ": §lLISTENING", x + 2, y + 8, defaultColor);
        } else if (value == -1) {
            Renderer2D.drawFixedString(drawContext.getMatrices(), name + ": None", x + 2, y + 8, defaultColor);
        } else {
            String keyName = KeyboardUtils.translate(value).toUpperCase(Locale.ROOT);
            Renderer2D.drawFixedString(drawContext.getMatrices(), name + ": " + keyName, x + 2, y + 8, defaultColor);
        }

        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 50) {
            Tooltip.tooltip.changeText(description);
        }
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
        int defaultColor = ColorManager.INSTANCE.defaultTextColor();

        if (isListening) {
            FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), name + ": §lLISTENING", x + 2, y + 6, defaultColor);
        } else if (value == -1) {
            FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), name + ": None", x + 2, y + 6, defaultColor);
        } else {
            String keyName = KeyboardUtils.translate(value).toUpperCase(Locale.ROOT);
            FontRenderers.Small_fxfontRenderer.drawString(drawContext.getMatrices(), name + ": " + keyName, x + 2, y + 6, defaultColor);
        }

        if (hovered(mouseX, mouseY)) {
            hovertimer++;
        } else {
            hovertimer = 0;
        }

        if (hovertimer >= 50) {
            Tooltip.tooltip.changeText(description);
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isListening) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.value = -1;
            } else {
                this.value = keyCode;
            }
            iSettingChange.onSettingChange(this);
            listening = false;
            listeningKey = false;
            listeningMouse = false;
            isListening = false;
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredSetting((int) mouseX, (int) mouseY) && hoveredOverReset(mouseX, mouseY)) {
            value = defaultValue;
            iSettingChange.onSettingChange(this);
            isListening = false;
            listeningKey = false;
            listeningMouse = false;
            listening = false;
        }

        if (listeningMouse && isListening) {
            value = button;
            iSettingChange.onSettingChange(this);
            isListening = !isListening;
            listening = !listening;
            listeningKey = false;
            listeningMouse = false;
        }
        if (hovered((int) mouseX, (int) mouseY) && button == 0 && !listeningMouse && !isListening) {
            listening = !listening;
            listeningKey = !listeningKey;
            isListening = true;
            listeningMouse = true;
        }
    }

    @Override
    public Object saveToFile(List<Object> objectList) {

        //The actual key code.
        return value;
    }

    @Override
    public void loadFromFile(Map<String, Object> MAP) {
        if (MAP.get(this.getSaveName()) == null) {
            value = defaultValue;
            return;
        }
        value = MathUtils.d2iSafe(MAP.get(this.getSaveName()));
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
    }

    public static class Builder extends SettingBuilder<Builder, Integer, KeyBind> {
        ISettingChange iSettingChange;

        public Builder() {
            super(-1);
        }

        public Builder onSettingChange(ISettingChange iSettingChange) {
            this.iSettingChange = iSettingChange;
            return this;
        }

        @Override
        public KeyBind build() {
            return new KeyBind(name, description, iSettingChange, value, shouldRender, defaultValue);
        }
    }
}
