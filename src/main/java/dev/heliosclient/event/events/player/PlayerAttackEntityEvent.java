package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Default is POST
 */
@Cancelable
public class PlayerAttackEntityEvent extends Event {
    private final PlayerEntity player;
    private final Entity target;

    public PlayerAttackEntityEvent(PlayerEntity player, Entity target) {
        this.player = player;
        this.target = target;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public Entity getTarget() {
        return target;
    }

    @Cancelable
    public static class PRE extends PlayerAttackEntityEvent {

        public PRE(PlayerEntity player, Entity target) {
            super(player, target);
        }
    }
}
