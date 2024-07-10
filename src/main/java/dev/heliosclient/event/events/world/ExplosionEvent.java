package dev.heliosclient.event.events.world;

import dev.heliosclient.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.explosion.Explosion;

public class ExplosionEvent extends Event {
    final LivingEntity causingEntity;
    final float power;
    final Explosion explosion;

    public ExplosionEvent(LivingEntity causingEntity, float power, Explosion explosion) {
        this.causingEntity = causingEntity;
        this.power = power;
        this.explosion = explosion;
    }

    public Explosion getExplosion() {
        return explosion;
    }

    public Entity getCausingEntity() {
        return causingEntity;
    }

    public float getPower() {
        return power;
    }
}
