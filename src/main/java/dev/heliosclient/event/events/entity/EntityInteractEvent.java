package dev.heliosclient.event.events.entity;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;

/**
 * Event fired on block interaction.
 */
@Cancelable
@LuaEvent("BlockInteractEvent")
public class EntityInteractEvent extends Event {
    private final Entity entity;
    private final Hand hand;


    public EntityInteractEvent(Entity entity, Hand hand) {
        this.entity = entity;
        this.hand = hand;
    }

    public Hand getHand() {
        return hand;
    }

    public Entity getEntity() {
        return entity;
    }
}
