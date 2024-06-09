package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.entity.player.PlayerEntity;

@Cancelable
public class PlayerJumpEvent extends Event {
    public final PlayerEntity player;

    public PlayerJumpEvent(PlayerEntity player) {
        this.player = player;
    }
}
