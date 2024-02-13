package dev.heliosclient.event.events.client;

import dev.heliosclient.event.Event;
import dev.heliosclient.ui.clickgui.gui.HudBox;

public class HitBoxIntersectEvent extends Event {
    private final HudBox hudBox;

    public HitBoxIntersectEvent(HudBox hudBox) {
        this.hudBox = hudBox;
    }

    public HudBox getHitbox() {
        return this.hudBox;
    }
}