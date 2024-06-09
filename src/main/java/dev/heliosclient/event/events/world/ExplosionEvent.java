package dev.heliosclient.event.events.world;

import dev.heliosclient.event.Event;
import net.minecraft.entity.Entity;

public class ExplosionEvent extends Event {
    final Entity entity;
    final float power;

    public ExplosionEvent(Entity entity, float power) {
        this.entity = entity;
        this.power = power;
    }

    public Entity getEntity() {
        return entity;
    }

    public float getPower() {
        return power;
    }
}
