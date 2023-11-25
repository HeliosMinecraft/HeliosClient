package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.entity.player.PlayerEntity;

@Cancelable

public class PlayerLeaveEvent extends Event {
    private final PlayerEntity player;

    public PlayerLeaveEvent(PlayerEntity player) {
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return player;
    }
}

