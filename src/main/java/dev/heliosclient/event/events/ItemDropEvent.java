package dev.heliosclient.event.events;

import dev.heliosclient.event.Event;
import net.minecraft.item.ItemStack;

public class ItemDropEvent implements Event {
    private final ItemStack stack;

    public ItemDropEvent(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }
}
