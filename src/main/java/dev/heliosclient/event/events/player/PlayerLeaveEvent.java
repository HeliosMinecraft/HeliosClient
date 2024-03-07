package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.entity.player.PlayerEntity;

@Cancelable
@LuaEvent("PlayerLeaveEvent")
public class PlayerLeaveEvent extends Event {
    private final PlayerEntity player;

    public PlayerLeaveEvent(PlayerEntity player) {
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return player;
    }
}

