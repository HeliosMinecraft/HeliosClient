package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.player.*;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.util.player.FreeCamEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);

    }

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
        if (EventManager.postEvent(new PlayerDamageEvent((PlayerEntity) (Object) this, source)).isCanceled()) {
            cir.cancel();
        }
    }

    @SuppressWarnings("all")
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onPlayerAttack(Entity target, CallbackInfo ci) {
        if ((PlayerEntity.class.cast(this) instanceof FreeCamEntity) && target == HeliosClient.MC.player) {
            ci.cancel();
        }
    }


    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    protected void clipAtLedge(CallbackInfoReturnable<Boolean> info) {
        ClipAtLedgeEvent event = new ClipAtLedgeEvent();
        EventManager.postEvent(event);
        if (event.isCanceled()) info.setReturnValue(event.isCanceled());
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    protected void onJump(CallbackInfo ci) {
        PlayerJumpEvent event = new PlayerJumpEvent((PlayerEntity) (Object) this);
        EventManager.postEvent(event);
        if (event.isCanceled()) ci.cancel();
    }
}
