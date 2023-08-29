package dev.heliosclient.module.settings;

import dev.heliosclient.module.Module_;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.KeycodeToString;
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
        if (!listening) {
        if (value != 0) {
            String keyName = KeycodeToString.translate(value);

            drawContext.drawText(textRenderer, name + ": " + keyName, x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor(), false);
        } else {
            drawContext.drawText(textRenderer, name + ": None", x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor(), false);
        }
        } else {
            drawContext.drawText(textRenderer, name + ": §lLISTENING", x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor(), false);
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
        if (listening)
        {
            if (keyCode != GLFW.GLFW_KEY_BACKSPACE && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            this.value = keyCode;
            } else {
                this.value = 0;
            }
            listening = false;
            module.onSettingChange(this);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button)
    {
        if (hovered((int)mouseX, (int)mouseY) && button == 0) {
            listening = !listening;
    }
    }
}
