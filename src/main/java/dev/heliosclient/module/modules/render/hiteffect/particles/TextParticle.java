package dev.heliosclient.module.modules.render.hiteffect.particles;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.modules.render.hiteffect.HitEffectParticle;
import dev.heliosclient.util.TimerUtils;
import dev.heliosclient.util.fontutils.FontRenderers;
import dev.heliosclient.util.render.Renderer3D;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Random;

public class TextParticle extends HitEffectParticle {
    public static Random rd = new Random();
    private final float finalScale;
    private final String text;
    private final Vec3d pos;
    public TimerUtils timer = new TimerUtils();
    private float scale;

    public TextParticle(String text, Vec3d pos, float finalScale, float time_in_seconds) {
        super((int) (time_in_seconds * 20) + rd.nextInt(11) /* 500 ms or 10 tick additional random delay*/);
        this.text = text;
        this.pos = pos;
        this.finalScale = finalScale;
        timer.startTimer();
    }

    @Override
    public void tick() {
        current_age++;
        if (current_age >= life) {
            scale -= HeliosClient.MC.getTickDelta();
            if (scale <= 0.0f) {
                discard();
            }
        } else {
            scale += HeliosClient.MC.getTickDelta();
            if (scale >= finalScale) {
                scale = finalScale;
            }
        }
    }

    @Override
    public void render(MatrixStack stack, Color color) {
        Renderer3D.drawText(FontRenderers.Large_fxfontRenderer, text, (float) pos.x, (float) pos.y, (float) pos.z, -FontRenderers.Large_fxfontRenderer.getStringWidth(text) / 2.0f, -FontRenderers.Large_fxfontRenderer.getStringHeight(text) / 2.0f, scale, color.getRGB());
    }

    @Override
    public void discard() {
        super.discard();
        timer.resetTimer();
    }
}
