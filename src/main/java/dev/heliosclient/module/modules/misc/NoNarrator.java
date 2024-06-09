package dev.heliosclient.module.modules.misc;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import net.minecraft.client.option.NarratorMode;

public class NoNarrator extends Module_ {
    public NoNarrator() {
        super("NoNarrator", "Disables narrator from the game and prevents it from interrupting you", Categories.MISC);
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        mc.options.getNarrator().setValue(NarratorMode.OFF);
    }
}
