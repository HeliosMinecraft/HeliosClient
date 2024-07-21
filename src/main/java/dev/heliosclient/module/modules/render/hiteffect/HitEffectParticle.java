package dev.heliosclient.module.modules.render.hiteffect;

import dev.heliosclient.util.ColorUtils;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public abstract class HitEffectParticle {
    public int life = 0;
    public int current_age = 0;
    public boolean isDiscarded = false;
    public boolean hasRandomColor = false;
    protected Color particleColor = Color.WHITE;


    public HitEffectParticle(int life, boolean hasRandomColor) {
        this.life = life;
        this.hasRandomColor = hasRandomColor;

        if(hasRandomColor)
            particleColor = ColorUtils.getRandomColor();
    }

    public void tick() {
        current_age++;
        if (current_age > life) {
            discard();
        }
    }

    public abstract void render(MatrixStack stack, Color color);

    public void discard() {
        isDiscarded = true;
    }
}
