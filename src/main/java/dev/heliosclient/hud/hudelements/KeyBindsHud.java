package dev.heliosclient.hud.hudelements;

import dev.heliosclient.hud.HudElement;
import dev.heliosclient.hud.HudElementData;
import dev.heliosclient.managers.ColorManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.KeyBind;
import dev.heliosclient.util.KeyboardUtils;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class KeyBindsHud extends HudElement {
    public static HudElementData<KeyBindsHud> DATA = new HudElementData<>("KeyBinds", "Shows the modules with set keyBinds", KeyBindsHud::new);

    public KeyBindsHud() {
        super(DATA);
        this.width = 50;
        this.height = Math.round(Renderer2D.getStringHeight());
    }

    @Override
    public void renderElement(DrawContext drawContext, TextRenderer textRenderer) {
        super.renderElement(drawContext, textRenderer);

        int width = 20, height = 0;
        int y = this.y + 1;
        for(Module_ module : ModuleManager.getModules()){
            if(module.keyBind.value == KeyBind.none()) continue;

            String text = module.name + (module.isActive()? ColorUtils.green : ColorUtils.darkRed)   +  " [" + KeyboardUtils.translateShort(module.keyBind.value) + "]";

            width = Math.round(Math.max(width, Renderer2D.getStringWidth(text) + 5));

            Renderer2D.drawString(drawContext.getMatrices(),text,x + 2,y, ColorManager.INSTANCE.hudColor);

            y +=  Math.round(Renderer2D.getStringHeight(text)) + 3;
            height += Math.round(Renderer2D.getStringHeight(text)) + 3;
        }

        this.width = width;
        this.height = height + 1;
    }
}
