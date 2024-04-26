package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;

@Cancelable
@LuaEvent("PlayerRespawnEvent")
public class PlayerRespawnEvent extends Event {
    private final PlayerEntity player;
    private final PlayerRespawnS2CPacket packet;

    public PlayerRespawnEvent(PlayerEntity player, PlayerRespawnS2CPacket packet) {
        this.player = player;
        this.packet = packet;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public PlayerRespawnS2CPacket getPacket() {
        return packet;
    }
}
