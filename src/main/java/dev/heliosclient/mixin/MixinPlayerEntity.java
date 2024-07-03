package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.player.*;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.movement.Sprint;
import dev.heliosclient.module.modules.world.SpeedMine;
import dev.heliosclient.util.BlockUtils;
import dev.heliosclient.util.player.FreeCamEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stat;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {
    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);

    }

    @Shadow
    public abstract void increaseStat(Stat<?> stat, int amount);

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

    @ModifyReturnValue(method = "getBlockBreakingSpeed", at = @At(value = "RETURN"))
    public float ongetBlockBreakingSpeed(float ogBreakSpeed, BlockState block) {
        if (!getWorld().isClient) return ogBreakSpeed;

        SpeedMine speedMine = ModuleManager.get(SpeedMine.class);
        if (!speedMine.isActive() || speedMine.mode.getOption() != SpeedMine.Mode.Modifier) return ogBreakSpeed;

        float breakSpeedMod = (float) (ogBreakSpeed * speedMine.modifier.value);

        if (HeliosClient.MC.crosshairTarget instanceof BlockHitResult bhr) {
            BlockPos pos = bhr.getBlockPos();
            if (speedMine.modifier.value < 1 || (BlockUtils.canBreakInstantly(block, breakSpeedMod) == BlockUtils.canBreakInstantly(block, ogBreakSpeed))) {
                return breakSpeedMod;
            } else {
                return (float) (0.9f / BlockUtils.calcBlockBreakingDelta2(HeliosClient.MC.world.getBlockState(pos), 1f));
            }
        }

        return ogBreakSpeed;
    }

    @WrapWithCondition(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private boolean keepSprint$setVelocity(PlayerEntity instance, Vec3d vec3d) {
        return ModuleManager.get(Sprint.class).shouldStopSprinting();
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V"))
    private void onAttack$setSprinting(PlayerEntity instance, boolean b) {
        instance.setSprinting(ModuleManager.get(Sprint.class).shouldStopSprinting());
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
