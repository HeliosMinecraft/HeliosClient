package dev.heliosclient.mixin;

import dev.heliosclient.event.events.render.EntityLabelRenderEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer<T extends Entity> {

    @Inject(at = @At("HEAD"),
            method = "renderLabelIfPresent",
            cancellable = true)
    private void onRenderLabelIfPresent(T entity, Text text, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (EventManager.postEvent(new EntityLabelRenderEvent(entity, text, matrixStack, vertexConsumerProvider, i)).isCanceled()) {
            ci.cancel();
        }
    }
}
