package dev.heliosclient.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.util.render.Renderer3D;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At("HEAD"))
    public void onRenderPre(Entity entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        Renderer3D.renderThroughWalls();
        Renderer3D.setup();
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderPost(Entity entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        Renderer3D.cleanup();
        Renderer3D.stopRenderingThroughWalls();
    }
}

