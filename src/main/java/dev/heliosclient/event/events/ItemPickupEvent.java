package dev.heliosclient.event.events;

import dev.heliosclient.event.Event;
import net.minecraft.item.ItemStack;

public class ItemPickupEvent implements Event {
    private final ItemStack stack;

    public ItemPickupEvent(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }
}

