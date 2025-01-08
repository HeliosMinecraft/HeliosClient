package dev.heliosclient.module.modules.render.hiteffect.particles;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.modules.render.hiteffect.HitEffectParticle;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.fontutils.fxFontRenderer;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.timer.TimerUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Random;

public class TextParticle extends HitEffectParticle {
    public static Boolean COMICAL = true;
    public static Random rd = new Random();
    private final float finalScale;
    private final String text;
    private final Vec3d pos;
    public TimerUtils timer = new TimerUtils();
    private float scale;

    public TextParticle(String text, Vec3d pos, float finalScale, float time_in_seconds,boolean hasRandomColor) {
        super((int) (time_in_seconds * 20) + rd.nextInt(11) /* 500 ms or 10 tick additional random delay*/,hasRandomColor);
        this.text = text;
        this.pos = pos;
        this.finalScale = finalScale;
        timer.startTimer();
    }

    @Override
    public void tick() {
        current_age++;
        if (current_age >= life) {
            scale = Math.max(scale - HeliosClient.MC.getRenderTickCounter().getLastFrameDuration(), 0);
            if (scale == 0.0) {
                discard();
            }
        } else {
            scale += HeliosClient.MC.getRenderTickCounter().getLastFrameDuration();
            if (scale >= finalScale) {
                scale = finalScale;
            }
        }
    }

    @Override
    public void render(MatrixStack stack, Color color) {
        //Random is best for comical fontRenderer
        this.particleColor = hasRandomColor ? particleColor : color;

        fxFontRenderer fontRenderer = COMICAL && FontRenderers.Comical_fxfontRenderer != null? FontRenderers.Comical_fxfontRenderer : FontRenderers.Large_fxfontRenderer;

                                                                    //Levitating effect
        Renderer3D.drawText(fontRenderer, text, (float) pos.x, (float) pos.y + (current_age/10.0f) * 0.032f, (float) pos.z, -fontRenderer.getStringWidth(text) / 2.0f, -fontRenderer.getStringHeight(text) / 2.0f, scale, particleColor.getRGB());
    }

    @Override
    public void discard() {
        super.discard();
        timer.resetTimer();
    }
}
