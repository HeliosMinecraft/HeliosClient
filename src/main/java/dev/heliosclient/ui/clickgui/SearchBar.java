package dev.heliosclient.ui.clickgui;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.inputbox.InputBox;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class SearchBar extends InputBox {
    public SearchBar() {
        super(133, 16, "", 50, InputMode.ALL);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        update(x, y);

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x - 15 + 1.5f, y - 0.5f, width + 14f, height + 1f, 3, 0.5f, focused ? Color.WHITE.getRGB() : ColorUtils.argbToRgb(ColorManager.INSTANCE.clickGuiPrimary));
        Renderer2D.drawRoundedRectangleWithShadow(drawContext.getMatrices(), x - 15 + 2, y, width + 13f, height, 2, 6, ColorUtils.argbToRgb(ColorManager.INSTANCE.clickGuiPrimary,200));
        FontRenderers.Large_iconRenderer.drawString(drawContext.getMatrices(), "\uEA17", x - 15 + 4, y + 1, ColorManager.INSTANCE.defaultTextColor);

        float textHeight = Renderer2D.getFxStringHeight();
        float textY = y + (height - textHeight) / 2; // Center the text vertically

        if (focused) {
            scrollOffset = Math.max(0, Math.min(scrollOffset, value.length()));
            cursorPosition = Math.max(0, Math.min(cursorPosition, value.length()));
            displaySegment(drawContext, textY, textHeight);
        }
        if (!focused) {
            if (value.trim().isEmpty() || value.trim().equals(" ")) {
                if (Renderer2D.isVanillaRenderer()) {
                    drawContext.drawText(textRenderer, "Search...", x + 5, (int) (textY + 1), 0xFFAAAAAA, false);
                } else {
                    FontRenderers.Mid_fxfontRenderer.drawString(drawContext.getMatrices(), "Search...", x + 5, Renderer2D.isVanillaRenderer() ? textY + 1 : textY, 0xFFAAAAAA);
                }
            } else {
                displayFirstSegment(drawContext, textY);
            }
        }

        drawSelectionBox(drawContext, textY, textHeight);
    }


}
