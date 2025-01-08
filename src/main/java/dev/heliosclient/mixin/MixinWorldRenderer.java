package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.BlockSelection;
import dev.heliosclient.module.modules.render.ESP;
import dev.heliosclient.module.modules.render.Freecam;
import dev.heliosclient.module.modules.render.NoRender;
import dev.heliosclient.util.color.ColorUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void onDrawHighlightedBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, int color, CallbackInfo ci) {
        if (ModuleManager.get(BlockSelection.class).isActive())
            ci.cancel();
    }

    @Redirect(method = "getEntitiesToRender", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/Camera;getFocusedEntity()Lnet/minecraft/entity/Entity;"))
    private Entity allowRenderingClientPlayerInFreeCameraMode(Camera camera) {
        if (ModuleManager.get(Freecam.class).isActive()) {
            return MinecraftClient.getInstance().player;
        }

        return camera.getFocusedEntity();
    }

    @Inject(method = "renderEntity", at = @At("HEAD"))
    private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (entity != null) {
            if (!matrices.isEmpty()) {
                matrices.pop();
            }
            ESP esp = ModuleManager.get(ESP.class);
            if (esp.isActive()) {
                try {
                    matrices.push();
                    if (vertexConsumers instanceof OutlineVertexConsumerProvider outlineVertexConsumers) {
                        int color = esp.getColor(entity);
                        outlineVertexConsumers.setColor(ColorUtils.getRed(color), ColorUtils.getGreen(color), ColorUtils.getBlue(color), ColorUtils.getAlpha(color));
                    }
                } finally {
                    matrices.pop();
                }
            }
        }
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void onRenderWeather(FrameGraphBuilder frameGraphBuilder, Vec3d pos, float tickDelta, Fog fog, CallbackInfo ci) {
        if (NoRender.get().isActive() && NoRender.get().noWeather.value) ci.cancel();
    }

    @Inject(method = "hasBlindnessOrDarkness(Lnet/minecraft/client/render/Camera;)Z", at = @At("HEAD"), cancellable = true)
    private void hasBlindnessOrDarkness(Camera camera, CallbackInfoReturnable<Boolean> info) {
        if (NoRender.get().isActive()) {
            if (NoRender.get().noBlindness.value || NoRender.get().noDarkness.value)
                info.setReturnValue(null);
        }
    }


}
