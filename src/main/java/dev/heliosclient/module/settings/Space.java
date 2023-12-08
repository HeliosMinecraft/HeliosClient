package dev.heliosclient.module.settings;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.BooleanSupplier;

public class Space extends Setting<Boolean> {

    public Space(int height, BooleanSupplier shouldRender, boolean defaultValue) {
        super(shouldRender, defaultValue);
        this.height = height;
        this.heightCompact = height;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
    }

}
