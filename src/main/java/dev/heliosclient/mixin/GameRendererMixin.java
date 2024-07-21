package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.player.NoMiningTrace;
import dev.heliosclient.module.modules.render.NoRender;
import dev.heliosclient.module.modules.render.Zoom;
import dev.heliosclient.module.modules.world.LiquidInteract;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    MinecraftClient client;
    @Mutable
    @Shadow
    @Final
    private Camera camera;

    // MeteorClient.com
    @Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = {"ldc=hand"}), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void render(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        camera = client.gameRenderer.getCamera();
        EventManager.postEvent(Render3DEvent.get(matrices, tickDelta, camera.getPos().x, camera.getPos().y, camera.getPos().z));
    }

    @Inject(method = "bobView", at = @At(value = "HEAD"), cancellable = true)
    private void cancelBobView(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        ci.cancel();
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/RotationAxis;rotationDegrees(F)Lorg/joml/Quaternionf;"), method = "bobView", require = 1)
    public float changeBobIntensity(float value) {
        return 0.0F;
    }

    @Inject(at = @At(value = "RETURN", ordinal = 1),
            method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D",
            cancellable = true)
    private void setFovForZoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        if (ModuleManager.get(Zoom.class).isActive()) {
            cir.setReturnValue(ModuleManager.get(Zoom.class).getZoomAmount());
        }
    }

    @Inject(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"), cancellable = true)
    private void onUpdateTargetedEntity(float tickDelta, CallbackInfo info) {
        if (ModuleManager.get(NoMiningTrace.class).shouldRemoveTrace() && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            client.getProfiler().pop();
            info.cancel();
        }
    }

    @Redirect(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"))
    private HitResult redirectCrosshairTargetValue(Entity entity, double maxDistance, float tickDelta, boolean includeFluids) {
        if (ModuleManager.get(LiquidInteract.class).isActive()) {
            HitResult result = entity.raycast(maxDistance, tickDelta, includeFluids);
            if (result.getType() != HitResult.Type.MISS) return result;

            return entity.raycast(maxDistance, tickDelta, true);
        }
        return entity.raycast(maxDistance, tickDelta, includeFluids);
    }


    @ModifyExpressionValue(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float applyCameraTransformationsMathHelperLerpProxy(float original) {
        return NoRender.get().isActive() && NoRender.get().noNausea.value ? 0 : original;
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo info) {
        if (floatingItem.getItem() == Items.TOTEM_OF_UNDYING && NoRender.get().isActive() && NoRender.get().noTotemAnimation.value) {
            info.cancel();
        }
    }

}
