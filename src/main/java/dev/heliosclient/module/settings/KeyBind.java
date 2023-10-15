package dev.heliosclient.module.settings;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.FontManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.KeycodeToString;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public class KeyBind extends Setting {

    public int value;
    public boolean listening = false;
    Module_ module;

    public KeyBind(String name, String description, Module_ module, Integer value) {
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
            Renderer2D.drawFixedString(drawContext.getMatrices(),name + ": §lLISTENING", x + 2, y + 8,defaultColor);
        } else if (value == 0) {
            Renderer2D.drawFixedString(drawContext.getMatrices(),name + ": None", x + 2, y + 8,defaultColor);
        } else {
            String keyName = KeycodeToString.translate(value);
            Renderer2D.drawFixedString(drawContext.getMatrices(),name + ": "+keyName, x + 2, y + 8,defaultColor);
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
        this.render(drawContext, x, y, mouseX, mouseY, textRenderer);
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
            module.onSettingChange(this);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered((int) mouseX, (int) mouseY) && button == 0) {
            listening = !listening;
        }
    }
}
