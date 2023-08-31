package dev.heliosclient.module.settings;

import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.KeyPressedEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.util.InputBox;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class StringSetting extends Setting implements Listener {
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
        EventManager.register(this);
    }


    @Override
    public void render(DrawContext drawContext, int x, int y,int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 5, ColorManager.INSTANCE.defaultTextColor(), false);
        inputBox.render(drawContext,x ,y,mouseX,mouseY,textRenderer);
    }


    @SubscribeEvent
    public void keyPressed(KeyPressedEvent keyPressedEvent) {
        inputBox.keyPressed(keyPressedEvent.getKey(), keyPressedEvent.getScancode(),keyPressedEvent.getModifiers());
    }

    @Override
    public void keyReleased(int keyCode,int Scancode, int modifiers) {
        inputBox.keyReleased(keyCode,Scancode,modifiers);
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



