package dev.heliosclient.event.events.entity;

import dev.heliosclient.event.Event;
import net.minecraft.entity.Entity;

public class EntityAddedEvent extends Event {
    public final Entity entity;

    public EntityAddedEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
