package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.item.ItemStack;

@Cancelable
@LuaEvent("ItemPickupEvent")
public class ItemPickupEvent extends Event {
    private final ItemStack stack;

    public ItemPickupEvent(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }
}

