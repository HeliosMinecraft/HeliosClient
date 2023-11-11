package dev.heliosclient.event.events;

import dev.heliosclient.event.Event;
import dev.heliosclient.ui.clickgui.gui.Hitbox;

public class HitBoxIntersectEvent extends Event {
    private final Hitbox hitbox;

    public HitBoxIntersectEvent(Hitbox hitbox) {
        this.hitbox = hitbox;
    }

    public Hitbox getHitbox() {
        return this.hitbox;
    }
}