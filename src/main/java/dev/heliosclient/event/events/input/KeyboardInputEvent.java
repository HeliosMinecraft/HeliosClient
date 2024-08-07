package dev.heliosclient.event.events.input;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;

@Cancelable
public class KeyboardInputEvent extends Event {
    public float movementSideways;
    public float movementForward;
    public boolean pressingForward;
    public boolean pressingBack;
    public boolean pressingLeft;
    public boolean pressingRight;
    public boolean jumping;
    public boolean sneaking;

    public KeyboardInputEvent(float movementSideways, float movementForward, boolean pressingForward, boolean pressingBack, boolean pressingLeft, boolean pressingRight, boolean jumping, boolean sneaking) {
        this.movementSideways = movementSideways;
        this.movementForward = movementForward;
        this.pressingForward = pressingForward;
        this.pressingBack = pressingBack;
        this.pressingLeft = pressingLeft;
        this.pressingRight = pressingRight;
        this.jumping = jumping;
        this.sneaking = sneaking;
    }

    public void set(float movementSideways, float movementForward, boolean pressingForward, boolean pressingBack, boolean pressingLeft, boolean pressingRight, boolean jumping, boolean sneaking) {
        this.movementSideways = movementSideways;
        this.movementForward = movementForward;
        this.pressingForward = pressingForward;
        this.pressingBack = pressingBack;
        this.pressingLeft = pressingLeft;
        this.pressingRight = pressingRight;
        this.jumping = jumping;
        this.sneaking = sneaking;
    }

    public boolean isSame(float movementSideways, float movementForward, boolean pressingForward, boolean pressingBack, boolean pressingLeft, boolean pressingRight, boolean jumping, boolean sneaking){
        return this.movementForward == movementForward && this.movementSideways == movementSideways && this.pressingForward == pressingForward && this.pressingBack == pressingBack && this.pressingRight == pressingRight && this.jumping == jumping && this.sneaking == sneaking && this.pressingLeft == pressingLeft;
    }

    public void setNone() {
        this.set(0f,0f,false,false,false,false,false,false);
    }

}
