package dev.heliosclient.module.settings;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.KeycodeToString;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.interfaces.ISettingChange;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class KeyBind extends Setting<Integer> {
    public static boolean listeningKey = false;
    public static boolean listeningMouse = false;
    public int value;
    public boolean listening = false;

    public KeyBind(String name, String description, ISettingChange iSettingChange, Integer value, BooleanSupplier shouldRender, int defaultValue) {
        super(shouldRender, defaultValue);
        this.iSettingChange = iSettingChange;
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
            listeningMouse = false;
            iSettingChange.onSettingChange(this);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (listeningMouse && listening) {
            value = button;
            iSettingChange.onSettingChange(this);
            listening = !listening;
            listeningKey = false;
            listeningMouse = false;
        }
        if (hovered((int) mouseX, (int) mouseY) && button == 0 && !listeningMouse) {
            listening = !listening;
            listeningKey = !listeningKey;
            listeningMouse = true;
        }
    }

    @Override
    public Object saveToToml(List<Object> objectList) {
        return value == -1 ? "None" : (char) value;
    }

    @Override
    public void loadFromToml(Map<String, Object> MAP, Toml toml) {
        super.loadFromToml(MAP,toml);
        if(toml.getString(name.replace(" ", "")) == null){
            value = defaultValue;
            return;
        }
        if (Objects.equals(toml.getString(name.replace(" ", "")), "None")) {
            value = -1;
        } else {
            char ch = toml.getString(name.replace(" ", "")).toLowerCase().charAt(0);
            value = KeycodeToString.charToGLFWKeycode(ch);
        }
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
