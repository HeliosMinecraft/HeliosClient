package dev.heliosclient.module.settings;

import dev.heliosclient.module.Module_;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

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
        this.value = value;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) 
    {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        if (this.quickSettings) {
            drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor(), false);
            drawContext.fill(x + moduleWidth - 20, y + 7, x + moduleWidth - 10, y + 17, 0xFFFFFFFF);
            drawContext.fill(x + moduleWidth - 19, y + 8, x + moduleWidth - 11, y + 16, 0xFF222222);
            drawContext.fill(x + moduleWidth - 18, y + 9, x + moduleWidth - 12, y + 15, value ? 0xFF55FFFF : 0xFF222222);
        } else {
            drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 8, ColorManager.INSTANCE.defaultTextColor(), false);
            drawContext.fill(x + 175, y + 7, x + 185, y + 17, 0xFFFFFFFF);
            drawContext.fill(x + 176, y + 8, x + 184, y + 16, 0xFF222222);
            drawContext.fill(x + 177, y + 9, x + 183, y + 15, value ? 0xFF55FFFF : 0xFF222222);
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
	public void mouseClicked(double mouseX, double mouseY, int button) 
    {
		if (hovered((int)mouseX, (int)mouseY) && button == 0) 
        {
			this.value = !value;
            module.onSettingChange(this);
		}
	}
}
