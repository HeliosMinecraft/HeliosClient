package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.player.ItemDropEvent;
import dev.heliosclient.event.events.player.PlayerDamageEvent;
import dev.heliosclient.event.events.player.PlayerDeathEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {


    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"))
    private void dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        ItemDropEvent event = new ItemDropEvent(stack);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            cir.cancel();
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        PlayerDeathEvent event = new PlayerDeathEvent(HeliosClient.MC.player);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onPlayerDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // This variable holds the event object
        if(EventManager.postEvent(new PlayerDamageEvent((PlayerEntity) (Object) this, source)).isCanceled()){
            cir.cancel();
        }
    }
}
