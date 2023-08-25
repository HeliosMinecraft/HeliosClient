package dev.heliosclient.mixin;

import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.events.ItemPickupEvent;
import dev.heliosclient.event.events.PlayerDeathEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Inject(method = "sendPickup", at = @At("HEAD"))
    private void onItemPickup(Entity entity, int count, CallbackInfo info) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack stack = player.getInventory().getStack(player.getInventory().getEmptySlot());
        EventManager.postEvent(new ItemPickupEvent(stack));
    }
}

