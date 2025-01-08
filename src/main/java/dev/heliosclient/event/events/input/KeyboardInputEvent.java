package dev.heliosclient.event.events.input;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;

@Cancelable
public class KeyboardInputEvent extends Event {
    public boolean pressingForward;
    public boolean pressingBack;
    public boolean pressingLeft;
    public boolean pressingRight;
    public boolean jumping;
    public boolean sneaking;
    public boolean sprinting;
    public float movementForward = 0.0f, movementSideways = 0.0f;

    public KeyboardInputEvent(boolean pressingForward, boolean pressingBack, boolean pressingLeft, boolean pressingRight, boolean jumping, boolean sneaking, boolean sprinting) {
        this.pressingForward = pressingForward;
        this.pressingBack = pressingBack;
        this.pressingLeft = pressingLeft;
        this.pressingRight = pressingRight;
        this.jumping = jumping;
        this.sneaking = sneaking;
        this.sprinting = sprinting;
    }
    public void set( boolean pressingForward, boolean pressingBack, boolean pressingLeft, boolean pressingRight, boolean jumping, boolean sneaking, boolean sprinting) {
        this.pressingForward = pressingForward;
        this.pressingBack = pressingBack;
        this.pressingLeft = pressingLeft;
        this.pressingRight = pressingRight;
        this.jumping = jumping;
        this.sneaking = sneaking;
        this.sprinting = sprinting;
    }

    public boolean isSame(boolean pressingForward, boolean pressingBack, boolean pressingLeft, boolean pressingRight, boolean jumping, boolean sneaking){
        return this.pressingForward == pressingForward && this.pressingBack == pressingBack && this.pressingRight == pressingRight && this.jumping == jumping && this.sneaking == sneaking && this.pressingLeft == pressingLeft;
    }

    public void setNone() {
        this.set(false,false,false,false,false,false,false);
    }

    public boolean shouldApplyMovementForward(){
        return this.movementForward != 0.0f;
    }
    public boolean shouldApplyMovementSideways(){
        return this.movementSideways != 0.0f;
    }
}
