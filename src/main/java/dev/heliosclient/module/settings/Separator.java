package dev.heliosclient.module.settings;

import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.BooleanSupplier;

public class Separator extends Setting {
    public boolean value;

    public Separator(int height, BooleanSupplier shouldRender) {
        super(shouldRender);
        this.height = height;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 2, y + height / 2, width - 2, 1, 0xCCFFFFFF);
    }

    @Override
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        Renderer2D.drawRectangle(drawContext.getMatrices().peek().getPositionMatrix(), x + 2, y + heightCompact / 2, widthCompact - 2, 1, 0xCCFFFFFF);
    }

}
