package dev.heliosclient.mixin;

import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Mutable
    @Shadow
    @Final
    private Camera camera;

    @Shadow
    @Final
    MinecraftClient client;

    // MeteorClient.com
    @Inject(method = "renderWorld", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = {"ldc=hand"}), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void render(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        camera = client.gameRenderer.getCamera();
        Render3DEvent event = new Render3DEvent(matrices, tickDelta, camera.getPos().x, camera.getPos().y, camera.getPos().z);
        EventManager.postEvent(event);
    }

    @Inject(method = "bobView", at = @At(value = "HEAD"), cancellable = true)
    private void cancelBobView(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        ci.cancel();
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/RotationAxis;rotationDegrees(F)Lorg/joml/Quaternionf;"), method = "bobView", require = 1)
    public float changeBobIntensity(float value) {
        return 0.0F;
    }
}
