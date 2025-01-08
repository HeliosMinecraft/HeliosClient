package dev.heliosclient.event.events.world;

import dev.heliosclient.event.Event;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class ExplosionEvent extends Event {
    final Vec3d center;
    final Optional<Vec3d> playerKnockBack;
    final ParticleEffect explosionParticle;
    final float estimatedPower;

    public ExplosionEvent(Vec3d center, Optional<Vec3d> playerKnockBack, ParticleEffect explosionParticle) {
        this.center = center;
        this.playerKnockBack = playerKnockBack;
        this.explosionParticle = explosionParticle;
        this.estimatedPower = (float) Math.sqrt(playerKnockBack.map(Vec3d::length).orElse(0.0)) * 0.5f;
    }

    public float getEstimatedPower() {
        return estimatedPower;
    }

    public Optional<Vec3d> getPlayerKnockBack() {
        return playerKnockBack;
    }

    public ParticleEffect getExplosionParticle() {
        return explosionParticle;
    }

    public Vec3d getCenter() {
        return center;
    }
}
