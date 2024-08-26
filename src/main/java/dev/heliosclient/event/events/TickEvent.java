package dev.heliosclient.event.events;

import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Something really important
 *
 * <h1>
 * Note: The TickEvent class will not be posted by the EventManager. Instead, use the subclass CLIENT.
 * </h1>
 */
public abstract class TickEvent extends Event {
    private TickEvent(){}

    //Called for every Client tick
    @LuaEvent("ClientTick")
    public static class CLIENT extends TickEvent {
    }

    @LuaEvent("WorldTick")
    public static class WORLD extends TickEvent {
        //Called for every clientWorld Tick
    }

    @LuaEvent("PlayerTick")
    public static class PLAYER extends TickEvent {

        //Called for every client player alive tick

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
