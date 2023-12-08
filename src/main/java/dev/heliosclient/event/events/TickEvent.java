package dev.heliosclient.event.events;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

/**
 * Something really important
 */
@Cancelable
public class TickEvent extends Event {

    //Called for every Client tick
    public static class CLIENT extends TickEvent {
        private final MinecraftClient client;

        public CLIENT(MinecraftClient client) {
            this.client = client;
        }

        public MinecraftClient getClient() {
            return client;
        }
    }

    //Called for every SinglePlayer world Tick
    public static class WORLD extends TickEvent {
        private final MinecraftServer server;

        public WORLD(MinecraftServer server) {
            this.server = server;
        }

        public MinecraftServer getServer() {
            return server;
        }
    }

    //Called for every player alive tick
    public static class PLAYER extends TickEvent {
        private final PlayerEntity player;

        public PLAYER(PlayerEntity player) {
            this.player = player;
        }

        public PlayerEntity getPlayer() {
            return player;
        }
    }

}
