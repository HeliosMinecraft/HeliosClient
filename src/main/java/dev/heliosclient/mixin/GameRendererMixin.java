package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.player.NoMiningTrace;
import dev.heliosclient.module.modules.render.AspectRatio;
import dev.heliosclient.module.modules.render.NoRender;
import dev.heliosclient.module.modules.render.Zoom;
import dev.heliosclient.module.modules.world.LiquidInteract;
import dev.heliosclient.util.render.GradientBlockRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
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

    @Shadow private float zoom;

    @Shadow public abstract void render(float tickDelta, long startTime, boolean tick);

    @Shadow public abstract float getFarPlaneDistance();

    @Shadow private float zoomX;
    @Shadow private float zoomY;
    @Unique private float lastZoom;
    @Unique private boolean isZooming;


    // MeteorClient.com
    @Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = {"ldc=hand"}), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void render(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        camera = client.gameRenderer.getCamera();

        EventManager.postEvent(Render3DEvent.get(matrices, tickDelta, camera.getPos().x, camera.getPos().y, camera.getPos().z));

        GradientBlockRenderer.renderGradientBlocks();
    }

    @Inject(method = "getBasicProjectionMatrix", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;mul(Lorg/joml/Matrix4fc;)Lorg/joml/Matrix4f;"), cancellable = true)
    private void getBasicProjectionMatrix$ChangeAspectRatio(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        AspectRatio ratio = ModuleManager.get(AspectRatio.class);

        if (ratio.isActive()) {
            cir.cancel();
        }else{
            return;
        }

        MatrixStack matrixStack = new MatrixStack();
        matrixStack.peek().getPositionMatrix().identity();
        if (this.zoom != 1.0F) {
            matrixStack.translate(zoomX, -zoomY, 0.0F);
            matrixStack.scale(this.zoom, this.zoom, 1.0F);
        }
         float aspectRatio = (float) ratio.aspectRatio.value;
        float cameraDepth = (float) ratio.cameraDepth.value;

        matrixStack.peek().getPositionMatrix().mul((new Matrix4f()).setPerspective((float)(fov * 0.01745329238474369), aspectRatio, cameraDepth, this.getFarPlaneDistance()));

        cir.setReturnValue(matrixStack.peek().getPositionMatrix());
    }


    @Inject(method = "renderWorld", at = @At(value = "HEAD"))
    private void renderWorld$ChangeZoom(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        if (ModuleManager.get(Zoom.class).isActive()) {
            if(!isZooming){
                lastZoom = zoom;
                isZooming = true;
            }
            zoom = (float) ModuleManager.get(Zoom.class).getZoomAmount();
        }else{
            if(isZooming){
                zoom = lastZoom;
            }
            isZooming = false;
        }
    }

    @Inject(method = "bobView", at = @At(value = "HEAD"), cancellable = true)
    private void cancelBobView(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        ci.cancel();
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/RotationAxis;rotationDegrees(F)Lorg/joml/Quaternionf;"), method = "bobView", require = 1)
    public float changeBobIntensity(float value) {
        return 0.0F;
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
