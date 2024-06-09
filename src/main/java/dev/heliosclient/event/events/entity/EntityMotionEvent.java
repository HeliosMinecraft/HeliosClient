package dev.heliosclient.event.events.entity;

import dev.heliosclient.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

public class EntityMotionEvent extends Event {
    private final MovementType type;
    private final Vec3d movement;
    private final Entity entity;

    public EntityMotionEvent(MovementType type, Vec3d movement, Entity entity) {
        this.type = type;
        this.movement = movement;
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public MovementType getType() {
        return type;
    }

    public Vec3d getMovement() {
        return movement;
    }
}
