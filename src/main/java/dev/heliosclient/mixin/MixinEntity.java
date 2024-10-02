package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.entity.EntityMotionEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.movement.NoFall;
import dev.heliosclient.module.modules.movement.NoSlow;
import dev.heliosclient.module.modules.render.FreeLook;
import dev.heliosclient.module.modules.render.Freecam;
import dev.heliosclient.module.modules.render.NoRender;
import dev.heliosclient.util.entity.FreeCamEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.heliosclient.util.render.Renderer3D.mc;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public float prevYaw;
    @Shadow
    public float prevPitch;
    @Shadow
    private float yaw;
    @Shadow
    private float pitch;
    @Unique
    private double forcedPitch;
    @Unique
    private double forcedYaw;

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    public abstract void setVelocity(Vec3d velocity);

    @Shadow public abstract void readNbt(NbtCompound nbt);

    @Inject(method = "move", at = @At(value = "HEAD"), cancellable = true)
    public void onMove(MovementType type, Vec3d movement, CallbackInfo ci) {
        if (EventManager.postEvent(new EntityMotionEvent(type, movement, (Entity) ((Object) this))).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "isTouchingWater", at = @At(value = "HEAD"), cancellable = true)
    private void isTouchingWater(CallbackInfoReturnable<Boolean> info) {
        if ((Object) this == mc.player && NoSlow.get().isActive() && NoSlow.get().fluidDrag.value)
            info.setReturnValue(false);
    }

    @Inject(method = "isInLava", at = @At(value = "HEAD"), cancellable = true)
    private void isInLava(CallbackInfoReturnable<Boolean> info) {
        if ((Object) this == mc.player && NoSlow.get().isActive() && NoSlow.get().fluidDrag.value)
            info.setReturnValue(false);
    }


    @ModifyExpressionValue(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSubmergedInWater()Z"))
    private boolean isSubmergedInWater(boolean submerged) {
        if ((Object) this == mc.player && NoSlow.get().isActive() && NoSlow.get().fluidDrag.value) return false;
        return submerged;
    }

    @ModifyReturnValue(method = "bypassesLandingEffects", at = @At("RETURN"))
    private boolean cancelBounce(boolean original) {
        return ModuleManager.get(NoFall.class).cancelBounce.value || original;
    }

    @ModifyReturnValue(method = "isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("RETURN"))
    private boolean isInvisibleToCanceller(boolean original) {
        if (!HeliosClient.shouldUpdate()) return original;
        if (NoRender.get().isActive() && NoRender.get().noInvisible.value) return false;
        return original;
    }

    @Inject(method = "updateVelocity", at = @At("HEAD"), cancellable = true)
    private void moreAccurateMoveRelative(float float_1, Vec3d motion, CallbackInfo ci) {
        if (ModuleManager.get(Freecam.class).isActive()) {
            FreeCamEntity camera = FreeCamEntity.getCamEntity();

            if (camera != null) {
                this.setVelocity(this.getVelocity().multiply(1D, 0D, 1D));
                ci.cancel();
            }
        }
    }

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void overrideYaw(double yawChange, double pitchChange, CallbackInfo ci) {
        FreeLook freeLook = ModuleManager.get(FreeLook.class);

        if (ModuleManager.get(Freecam.class).isActive()) {
            this.yaw = this.prevYaw;
            this.pitch = this.prevPitch;

            this.updateCustomRotations(yawChange, pitchChange);

            FreeCamEntity camera = FreeCamEntity.getCamEntity();

            if (camera != null) {
                camera.setRotations((float) this.forcedYaw, (float) this.forcedPitch);
            }

            ci.cancel();

            return;
        }

        if (freeLook != null && freeLook.isActive()) {
            freeLook.cameraYaw += (float) (yawChange * freeLook.sensitivity.value/2);
            freeLook.cameraPitch += (float) (pitchChange * freeLook.sensitivity.value/2);

            freeLook.cameraPitch = MathHelper.clamp(freeLook.cameraPitch, -90.0F, 90.0F);
            ci.cancel();
            return;
        }


        this.forcedYaw = this.yaw;
        this.forcedPitch = this.pitch;
    }

    @Unique
    private void updateCustomRotations(double yawChange, double pitchChange) {
        this.forcedYaw += yawChange * 0.15D;

        this.forcedPitch = net.minecraft.util.math.MathHelper.clamp(this.forcedPitch + pitchChange * 0.15D, -(float) 90, (float) 90);
    }
}
