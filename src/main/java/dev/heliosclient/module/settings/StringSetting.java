package dev.heliosclient.module.settings;

import dev.heliosclient.system.ColorManager;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.*;

public class StringSetting extends Setting {
    public String value;
    String description;
    private InputBox inputBox;
    private int characterLimit;

    public StringSetting(String name, String description, String value,int characterLimit) {
        this.name = name;
        this.value = value;
        this.description = description;
        this.height = 38;
        this.characterLimit=characterLimit;
        inputBox = new InputBox(180,13,value,characterLimit);
    }


    @Override
    public void render(DrawContext drawContext, int x, int y,int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 5, ColorManager.INSTANCE.defaultTextColor(), false);
        inputBox.render(drawContext,x ,y,mouseX,mouseY,textRenderer);
    }


    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        inputBox.keyPressed(keyCode, scanCode,modifiers);
    }

    @Override
    public void keyReleased(int keyCode, int scanCode, int modifiers) {
        super.keyReleased(keyCode, scanCode, modifiers);
        inputBox.keyReleased(keyCode,scanCode,modifiers);
    }

    public void charTyped(char chr, int modifiers) {
        inputBox.charTyped(chr, modifiers);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        inputBox.setFocused(hovered((int) mouseX, (int) mouseY));
    }
}



