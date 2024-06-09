package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import net.minecraft.entity.effect.StatusEffects;

public class NoLevitation extends Module_ {
    public NoLevitation() {
        super("No Levitation", "Removes levitation effect", Categories.MOVEMENT);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION)) {
            mc.player.removeStatusEffect(StatusEffects.LEVITATION);
        }
    }
}
