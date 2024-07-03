package dev.heliosclient.event.events;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Something really important
 */
@Cancelable
@LuaEvent("TickEvent")
public class TickEvent extends Event {

    //Called for every Client tick
    @LuaEvent("ClientTick")
    public static class CLIENT extends TickEvent {
        private final MinecraftClient client;

        public CLIENT(MinecraftClient client) {
            this.client = client;
        }

        public MinecraftClient getClient() {
            return client;
        }
    }

    //Called for every world Tick
    @LuaEvent("WorldTick")
    /**
     * Note only works for single player worlds!!!
     */
    public static class WORLD extends TickEvent {
    }

    //Called for every client player alive tick
    @LuaEvent("PlayerTick")
    public static class PLAYER extends TickEvent {

        public static PLAYER INSTANCE = new PLAYER();

        private PlayerEntity player;

        public static PLAYER get(PlayerEntity player) {
            INSTANCE.player = player;
            return INSTANCE;
        }

        public PlayerEntity getPlayer() {
            return player;
        }
    }

}
