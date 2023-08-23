package dev.heliosclient.module.settings;

import dev.heliosclient.module.Module_;
import dev.heliosclient.system.ColorManager;
import dev.heliosclient.ui.clickgui.Tooltip;
import dev.heliosclient.util.MathUtils;
import dev.heliosclient.util.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class DoubleSetting extends Setting
{
    public double value;
    private final double min, max;
    private final int roundingPlace;
    Module_ module;
    String description;

    boolean sliding = false;

    public DoubleSetting(String name, String description, Module_ module, double value, double min, double max, int roundingPlace)
    {
        this.name = name;
        this.description = description;
        this.value = value;
        this.min = min;
        this.max = max;
        this.module = module;
        this.roundingPlace = roundingPlace;
    }

    @Override
    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY, TextRenderer textRenderer) {
        super.render(drawContext, x, y, mouseX, mouseY, textRenderer);
        if (quickSettings) {
            drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 2, ColorManager.INSTANCE.defaultTextColor(), false);
            double diff = Math.min(moduleWidth - 10, Math.max(0, (mouseX - x)));

            if (sliding) {
                if (diff == 0) {
                    value = min;
                } else {
                    value = MathUtils.round(((diff / (moduleWidth - 10)) * (max - min) + min), roundingPlace);
                }
                module.onSettingChange(this);
            }

            String valueString = "" + MathUtils.round(value, roundingPlace);
            drawContext.drawText(textRenderer, Text.literal(valueString), (x + moduleWidth - 10) - textRenderer.getWidth(valueString), y + 2, ColorManager.INSTANCE.defaultTextColor(), false);
            Renderer2D.drawRoundedRectangle(drawContext,x + 2, y + 16, moduleWidth - 8, 2, 1,0xFFAAAAAA);
            int scaledValue = (int) ((value - min) / (max - min) * (moduleWidth - 10)) + 2;
            Renderer2D.drawRoundedRectangle(drawContext,x + 2, y + 16, scaledValue, 2,1 ,0xFF55FFFF);
            Renderer2D.drawRoundedRectangle(drawContext,x + scaledValue,y+14,2,6 ,1 ,0xFFFFFFFF);
        } else {
            drawContext.drawText(textRenderer, Text.literal(name), x + 2, y + 2, ColorManager.INSTANCE.defaultTextColor(), false);
            double diff = Math.min(100, Math.max(0, (mouseX - x) / 1.9));

            if (sliding) {
                if (diff == 0) {
                    value = min;
                } else {
                    value = MathUtils.round(((diff / 100) * (max - min) + min), roundingPlace);
                }
                module.onSettingChange(this);
            }

            String valueString = "" + MathUtils.round(value, roundingPlace);
            drawContext.drawText(textRenderer, Text.literal(valueString), (x + 190) - textRenderer.getWidth(valueString), y + 2, ColorManager.INSTANCE.defaultTextColor(), false);
            Renderer2D.drawRoundedRectangle(drawContext,x + 2,y+16 ,188 ,2 ,1 ,0xFFAAAAAA);
            int scaledValue = (int) ((value - min) / (max - min) * 188) + 2;
            Renderer2D.drawRoundedRectangle(drawContext,x+2,y+16,scaledValue,2 ,1 ,0xFF55FFFF);
            Renderer2D.drawRoundedRectangle(drawContext,x+scaledValue,y+14,2, 6,1 ,0xFFFFFFFF);
        }
        if (hovered(mouseX,mouseY)) {
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
			this.sliding = true;
		}
	}
	
	@Override
	public void mouseReleased(double mouseX, double mouseY, int button) 
    {
		sliding = false;
	}
    
}
