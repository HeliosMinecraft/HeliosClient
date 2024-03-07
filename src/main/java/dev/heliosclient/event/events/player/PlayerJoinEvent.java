package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.entity.player.PlayerEntity;

@Cancelable
@LuaEvent("PlayerJoinEvent")
public class PlayerJoinEvent extends Event {
    private final PlayerEntity player;

    public PlayerJoinEvent(PlayerEntity player) {
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return player;
    }
}
