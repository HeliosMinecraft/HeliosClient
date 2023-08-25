package dev.heliosclient.mixin;

import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.events.ItemPickupEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Shadow public abstract boolean shouldDropXp();

    @Inject(method = "sendPickup", at = @At("HEAD"), cancellable = true)
    private void onItemPickup(Entity entity, int count, CallbackInfo info) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack stack = player.getInventory().getStack(player.getInventory().getEmptySlot());
        ItemPickupEvent event =new ItemPickupEvent(stack);
        EventManager.postEvent(event);
        if(event.isCanceled()){
            info.cancel();
        }
    }
}

