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

    public KeyboardInputEvent(boolean pressingForward, boolean pressingBack, boolean pressingLeft, boolean pressingRight, boolean jumping, boolean sneaking) {
        this.pressingForward = pressingForward;
        this.pressingBack = pressingBack;
        this.pressingLeft = pressingLeft;
        this.pressingRight = pressingRight;
        this.jumping = jumping;
        this.sneaking = sneaking;
    }
    public void set( boolean pressingForward, boolean pressingBack, boolean pressingLeft, boolean pressingRight, boolean jumping, boolean sneaking) {
        this.pressingForward = pressingForward;
        this.pressingBack = pressingBack;
        this.pressingLeft = pressingLeft;
        this.pressingRight = pressingRight;
        this.jumping = jumping;
        this.sneaking = sneaking;
    }

    public boolean isSame(boolean pressingForward, boolean pressingBack, boolean pressingLeft, boolean pressingRight, boolean jumping, boolean sneaking){
        return this.pressingForward == pressingForward && this.pressingBack == pressingBack && this.pressingRight == pressingRight && this.jumping == jumping && this.sneaking == sneaking && this.pressingLeft == pressingLeft;
    }

    public void setNone() {
        this.set(false,false,false,false,false,false);
    }

}
