package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer2D;
import dev.heliosclient.util.render.Renderer3D;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;

import java.awt.*;

import static dev.heliosclient.HeliosClient.MC;

public class TntTimer extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    DoubleSetting scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("Scale")
            .description("Scale of the timer")
            .onSettingChange(this)
            .value(1d)
            .defaultValue(1d)
            .min(0)
            .max(3)
            .roundingPlace(1)
            .build()
    );

    public TntTimer() {
        super("TntTimer", "Displays time left on a exploding TNT", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent event) {
        for (Entity entity : MC.world.getEntities()) {
            if (entity instanceof TntEntity tnt) {
                String text = String.valueOf(tnt.getFuse() / 20.0f);
                Renderer3D.draw2DIn3D((float) tnt.getPos().x, (float) tnt.getEyeY() + 0.5f + (float) scale.value, (float) tnt.getPos().z, (float) scale.value, stack -> {
                    Renderer2D.drawRoundedRectangle(stack.peek().getPositionMatrix(), -(FontRenderers.Mid_fxfontRenderer.getStringWidth(text) / 2.0f) - 1f, 0, FontRenderers.Mid_fxfontRenderer.getStringWidth(text) + 2.0f, FontRenderers.Mid_fxfontRenderer.getStringHeight(text) + 0.2f, 3, Color.BLACK.getRGB());
                });
                Renderer3D.drawText(FontRenderers.Mid_fxfontRenderer, text, (float) tnt.getPos().x, (float) tnt.getEyeY() + 0.5f + (float) scale.value, (float) tnt.getPos().z, -(FontRenderers.Mid_fxfontRenderer.getStringWidth(text) / 2.0f), 0, (float) scale.value, getColor(tnt.getFuse()));
            }
        }
    }

    private int getColor(int fuseTime) {
        float ratio = fuseTime / 80.0f;
        int r, g, b;
        if (ratio > 0.75f) { // Green to Yellow
            r = (int) ((1.0f - (ratio - 0.75f) * 4) * 255);
            g = 255;
            b = 0;
        } else if (ratio > 0.5f) { // Yellow to Orange
            r = 255;
            g = (int) ((1.0f - (ratio - 0.5f) * 4) * 255);
            b = 0;
        } else if (ratio > 0.25f) { // Orange to Red
            r = 255;
            g = (int) ((ratio - 0.25f) * 4 * 255);
            b = 0;
        } else { // Red
            r = 255;
            g = 0;
            b = 0;
        }
        return 0xFF000000 | (r << 16) | (g << 8) | b; // Return as ARGB
    }
}
