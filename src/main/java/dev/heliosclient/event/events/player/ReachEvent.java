package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Event;

public class ReachEvent extends Event {
    private float reach;

    public ReachEvent(float reach) {
        this.reach = reach;
    }

    public float getReach() {
        return reach;
    }

    public void setReach(float reach) {
        this.reach = reach;
    }
}
