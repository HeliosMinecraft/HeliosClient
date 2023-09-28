package dev.heliosclient.mixin;

import dev.heliosclient.managers.EventManager;
import dev.heliosclient.event.events.ItemPickupEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity {

    @Inject(method = "sendPickup", at = @At("HEAD"), cancellable = true)
    private void onItemPickup(Entity item, int count, CallbackInfo info) {
        if (item instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getStack();
            ItemPickupEvent event = new ItemPickupEvent(stack);
            EventManager.postEvent(event);
            if (event.isCanceled()) {
                info.cancel();
            }
            //TODO: Future MobEntityPickUp Event
        }
    }
}

