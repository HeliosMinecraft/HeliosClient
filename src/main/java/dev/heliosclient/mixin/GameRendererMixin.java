package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.player.NoMiningTrace;
import dev.heliosclient.module.modules.render.AspectRatio;
import dev.heliosclient.module.modules.render.NoRender;
import dev.heliosclient.module.modules.render.Zoom;
import dev.heliosclient.module.modules.world.LiquidInteract;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.util.render.GradientBlockRenderer;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameRenderer.class,priority = 1002)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    private MinecraftClient client;
    @Mutable
    @Shadow
    @Final
    private Camera camera;

    @Shadow private float zoom;

    @Shadow public abstract float getFarPlaneDistance();

    @Shadow private float zoomX;
    @Shadow private float zoomY;
    @Shadow @Final private BufferBuilderStorage buffers;
    @Unique private float lastZoom;
    @Unique private boolean isZooming;


    // MeteorClient.com
    @Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = {"ldc=hand"}))
    private void render(RenderTickCounter tickCounter, CallbackInfo ci) {
        camera = client.gameRenderer.getCamera();

        EventManager.postEvent(Render3DEvent.get(new MatrixStack(), tickCounter.getTickDelta(false), camera.getPos().x, camera.getPos().y, camera.getPos().z));

        GradientBlockRenderer.renderGradientBlocks();
    }

    @Inject(method = "getBasicProjectionMatrix", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;perspective(FFFF)Lorg/joml/Matrix4f;"), cancellable = true)
    private void getBasicProjectionMatrix$ChangeAspectRatio(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
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

        matrixStack.peek().getPositionMatrix().mul((new Matrix4f()).setPerspective((float)(fovDegrees * 0.01745329238474369), aspectRatio, cameraDepth, this.getFarPlaneDistance()));

        cir.setReturnValue(matrixStack.peek().getPositionMatrix());
    }


    @Inject(method = "renderWorld", at = @At(value = "HEAD"))
    private void renderWorld$ChangeZoom(RenderTickCounter renderTickCounter, CallbackInfo ci) {
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
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderWithTooltip(Lnet/minecraft/client/gui/DrawContext;IIF)V",shift = At.Shift.AFTER))
    private void renderWorld$GlowMouse(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if(ClickGUI.shouldGlowMousePointer()) {
            DrawContext drawContext = new DrawContext(this.client, this.buffers.getEntityVertexConsumers());

            double i = this.client.mouse.getX() * (double) this.client.getWindow().getScaledWidth() / (double) this.client.getWindow().getWidth();
            double j = this.client.mouse.getY() * (double) this.client.getWindow().getScaledHeight() / (double) this.client.getWindow().getHeight();
            Renderer2D.drawCircularBlurredShadow(drawContext.getMatrices(), (float) i, (float) j,5.0f, ClickGUI.getGlowColor(), ClickGUI.getGlowRadius());
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

    @Inject(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"), cancellable = true)
    private void onUpdateTargetedEntity(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta, CallbackInfoReturnable<HitResult> cir) {
        if (ModuleManager.get(NoMiningTrace.class).shouldRemoveTrace() && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            //client.getProfiler().pop();
            cir.cancel();
        }
    }

    @Inject(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;", shift = At.Shift.AFTER))
    private void injectUpdateTargetedEntity(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta, CallbackInfoReturnable<HitResult> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        Entity entity = client.getCameraEntity();
        if (entity != null && client.world != null) {
            double d = client.player.getBlockInteractionRange();
            HitResult result = entity.raycast(d, tickDelta, false);

            if (ModuleManager.get(LiquidInteract.class).isActive() && result.getType() == HitResult.Type.MISS) {
                result = entity.raycast(d, tickDelta, true);
            }

            client.crosshairTarget = result;
        }
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
