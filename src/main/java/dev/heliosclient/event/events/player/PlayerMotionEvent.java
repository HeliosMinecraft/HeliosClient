package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import dev.heliosclient.system.mixininterface.IVec3d;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

@Cancelable
@LuaEvent("PlayerMotionEvent")
public class PlayerMotionEvent extends Event {
    private final MovementType type;
    private final Vec3d movement;

    public PlayerMotionEvent(MovementType type, Vec3d movement) {
        this.type = type;
        this.movement = movement;
    }

    public MovementType getType() {
        return type;
    }

    public Vec3d getMovement() {
        return movement;
    }

    public IVec3d modifyMovement(){
        return (IVec3d)movement;
    }
}
