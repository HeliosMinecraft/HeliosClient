package dev.heliosclient.mixin;


import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.misc.NoSwing;
import dev.heliosclient.module.modules.movement.AirJump;
import dev.heliosclient.module.modules.movement.NoJumpDelay;
import dev.heliosclient.module.modules.movement.Spider;
import dev.heliosclient.module.modules.render.ViewModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    @Shadow
    public boolean handSwinging;

    @Shadow
    public int handSwingTicks;

    @Shadow
    public Hand preferredHand;
    @Shadow
    protected boolean jumping;
    @Shadow
    private int jumpingCooldown;

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    protected abstract int getHandSwingDuration();

    @Shadow
    protected abstract void jump();

    @Inject(method = "isClimbing", at = @At(value = "HEAD"), cancellable = true)
    public void setClimbing(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == HeliosClient.MC.player) {
            if (ModuleManager.get(Spider.class).isActive() && HeliosClient.MC.player.horizontalCollision)
                cir.setReturnValue(true);
        }
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickEvent$NoJumpDelay(CallbackInfo callbackInfo) {
        // if both noJump delay and air jump is enabled then it will turn into a jetpack like situation
        boolean noJumpDelay = ModuleManager.get(NoJumpDelay.class).isActive() && !ModuleManager.get(AirJump.class).isActive();

        if (noJumpDelay) {
            jumpingCooldown = 0;
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;jumping:Z"))
    private void onTickEvent$AirJump(CallbackInfo callbackInfo) {
        if (ModuleManager.get(AirJump.class).isActive() && jumping && jumpingCooldown == 0) {
            this.jump();
            jumpingCooldown = 10;
        }
    }

    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;Z)V", at = @At("HEAD"), cancellable = true)
    private void onSwingHand(Hand hand, boolean fromServerPlayer, CallbackInfo ci) {
        NoSwing noSwing = ModuleManager.get(NoSwing.class);
        if (LivingEntity.class.cast(this) == HeliosClient.MC.player && noSwing.isActive() && noSwing.swingMode.getOption() != NoSwing.SwingMode.NoServer) {
            Hand newhand = noSwing.swingMode.getOption() == NoSwing.SwingMode.OffHand ? Hand.OFF_HAND : Hand.MAIN_HAND;
            if (!this.handSwinging || this.handSwingTicks >= this.getHandSwingDuration() / 2 || this.handSwingTicks < 0) {
                this.handSwingTicks = -1;
                this.handSwinging = noSwing.swingMode.getOption() != NoSwing.SwingMode.None;
                this.preferredHand = newhand;
                if (getWorld() instanceof ServerWorld sv) {
                    EntityAnimationS2CPacket entityAnimationS2CPacket = new EntityAnimationS2CPacket(this, newhand == Hand.MAIN_HAND ? 0 : 3);
                    ServerChunkManager serverChunkManager = sv.getChunkManager();
                    if (fromServerPlayer) {
                        serverChunkManager.sendToNearbyPlayers(this, entityAnimationS2CPacket);
                    } else {
                        serverChunkManager.sendToOtherNearbyPlayers(this, entityAnimationS2CPacket);
                    }
                }
            }
            ci.cancel();
        }
    }


    @ModifyConstant(method = "getHandSwingDuration", constant = @Constant(intValue = 6))
    private int onGetHandSwingDuration(int constant) {
        if ((Object) this != HeliosClient.MC.player) return constant;
        return ModuleManager.get(ViewModel.class).isActive() && HeliosClient.MC.options.getPerspective().isFirstPerson() ? (int) ModuleManager.get(ViewModel.class).swingSpeed.value : constant;
    }
}
