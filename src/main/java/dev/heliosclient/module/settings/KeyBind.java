package dev.heliosclient.module.settings;

import dev.heliosclient.util.KeycodeToString;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public class KeyBind extends Setting {

    public int value;
    public boolean listening = false;

    public KeyBind(String name, Integer value) {
        this.name = name;
        this.value = value;
        this.height = 24;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        if (!listening) {
        if (value != 0) {
            String keyName = KeycodeToString.translate(value);

            drawContext.drawTextWithShadow(textRenderer, name + ": " + keyName
, x + 2, y + 8, 0xFFFFFF);
        } else {
            drawContext.drawTextWithShadow(textRenderer, name + ": None", x + 2, y + 8, 0xFFFFFF);
        }
        } else {
            drawContext.drawTextWithShadow(textRenderer, name + ": §lLISTENING", x + 2, y + 8, 0xFFFFFF);
        }
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
