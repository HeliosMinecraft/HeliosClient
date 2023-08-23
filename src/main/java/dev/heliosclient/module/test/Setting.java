package dev.heliosclient.module.test;

import net.minecraft.client.gui.DrawContext;

// Setting class
public abstract class Setting<T> {
    protected String name;
    protected T value;
    protected int x, y, width, height;

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
        this.width = 80;
        this.height = 20;
    }

    public abstract void render(DrawContext drawContext, int x, int y);

    public abstract void mouseClicked(double mouseX, double mouseY, int button);
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
