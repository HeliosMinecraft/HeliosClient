package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.FreeLook;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow protected abstract void moveBy(float f, float g, float h);

    @Shadow protected abstract float clipToSpace(float f);

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void updateSetRotation(Args args) {
        FreeLook freeLook = ModuleManager.get(FreeLook.class);

        if (freeLook != null && freeLook.isActive()) {
            args.set(0, freeLook.cameraYaw);
            args.set(1, freeLook.cameraPitch);
        }
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;moveBy(FFF)V"))
    private void redirectUpdateMoveBy(Camera instance, float f, float g, float h) {
        FreeLook freeLook = ModuleManager.get(FreeLook.class);
        this.moveBy(-this.clipToSpace(freeLook.isActive() ? freeLook.getDistanceFromPlayer() : 4.0F), 0.0F, 0.0F);
    }
}
