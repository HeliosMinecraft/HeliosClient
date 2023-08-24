package dev.heliosclient.module.settings;

import dev.heliosclient.module.Module_;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.*;

public class BooleanSetting extends Setting
{
    public boolean value;
    Module_ module;
    String description;

    public BooleanSetting(String name, String description, Module_ module, boolean value)
    {
        this.module = module;
        this.name = name;
        this.description = description;
        this.heightCompact = 18;
        this.value = value;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) 
    {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor(), false);
        drawContext.fill(x + 175, y + 7, x + 185, y + 17, 0xFFFFFFFF);
        drawContext.fill(x + 176, y + 8, x + 184, y + 16, 0xFF222222);
        drawContext.fill(x + 177, y + 9, x + 183, y + 15, value ? 0xFF55FFFF : 0xFF222222);

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
    public void renderCompact(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer)
    {
        super.renderCompact(drawContext, x, y, mouseX, mouseY, textRenderer);
        drawContext.drawText(textRenderer, Text.literal(name), x + 3, y + 5, ColorManager.INSTANCE.defaultTextColor(), false);
        drawContext.fill(x + moduleWidth - 14, y + 4, x + moduleWidth - 4, y + 14, 0xFFFFFFFF);
        drawContext.fill(x + moduleWidth - 13, y + 5, x + moduleWidth - 5, y + 13, 0xFF222222);
        drawContext.fill(x + moduleWidth - 12, y + 6, x + moduleWidth - 6, y + 12, value ? 0xFF55FFFF : 0xFF222222);

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
	public void mouseClicked(double mouseX, double mouseY, int button) 
    {
		if (hovered((int)mouseX, (int)mouseY) && button == 0) 
        {
			this.value = !value;
            module.onSettingChange(this);
		}
	}
}
