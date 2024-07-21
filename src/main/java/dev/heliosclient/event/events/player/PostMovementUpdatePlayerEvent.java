package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;

@Cancelable
public class PostMovementUpdatePlayerEvent extends Event {
    public int numberOfTicks = 0;
    public boolean shiftedTicks = false;

    public void setNumberOfTicks(int numberOfTicks) {
        this.numberOfTicks = numberOfTicks;
    }

    public boolean hasShiftedTicks() {
        return shiftedTicks;
    }
}
