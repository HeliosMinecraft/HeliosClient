package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;

@Cancelable
@LuaEvent("PlayerJoinEvent")
public class PlayerJoinEvent extends Event {
    private final PlayerEntity player;
    private final GameJoinS2CPacket packet;


    public PlayerJoinEvent(PlayerEntity player, GameJoinS2CPacket packet) {
        this.player = player;
        this.packet = packet;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public GameJoinS2CPacket getPacket() {
        return packet;
    }
}
