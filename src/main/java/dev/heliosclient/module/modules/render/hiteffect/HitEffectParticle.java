package dev.heliosclient.module.modules.render.hiteffect;

import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public abstract class HitEffectParticle {
    public int life = 0;
    public int current_age = 0;
    public boolean isDiscarded = false;

    public HitEffectParticle(int life) {
        this.life = life;
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
