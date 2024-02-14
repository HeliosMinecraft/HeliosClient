package dev.heliosclient.ui.clickgui;

import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class SearchBar extends InputBox {
    public SearchBar() {
        super(146, 16, "", 25, InputMode.ALL);
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        update(x, y);

        Renderer2D.drawOutlineRoundedBox(drawContext.getMatrices().peek().getPositionMatrix(), x - 15 + 1.5f, y - 0.5f, width + 1f, height + 1f, 3, 0.5f, focused ? Color.WHITE.getRGB() : ColorUtils.changeAlpha(new Color(ColorManager.INSTANCE.clickGuiPrimary), 255).getRGB());
        Renderer2D.drawRoundedRectangleWithShadow(drawContext.getMatrices(), x - 15 + 2, y, width, height, 2, 6, ColorManager.INSTANCE.clickGuiPrimary);
        FontRenderers.Large_iconRenderer.drawString(drawContext.getMatrices(), "\uEA17", x - 15 + 4, y + 1, -1);

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
