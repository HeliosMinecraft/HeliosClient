package dev.heliosclient.event.events.entity;

import dev.heliosclient.event.Event;
import net.minecraft.entity.Entity;

public class EntityRemovedEvent extends Event {
    private final Entity entity;
    private final int entityId;
    private final Entity.RemovalReason removalReason;

    public EntityRemovedEvent(Entity entity, Entity.RemovalReason removalReason, int entityId) {
        this.entity = entity;
        this.removalReason = removalReason;
        this.entityId = entityId;
    }

    public Entity.RemovalReason getRemovalReason() {
        return removalReason;
    }

    public int getEntityId() {
        return entityId;
    }

    public Entity getEntity() {
        return entity;
    }
}
