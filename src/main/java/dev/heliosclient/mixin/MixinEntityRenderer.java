package dev.heliosclient.mixin;

import dev.heliosclient.event.events.render.EntityLabelRenderEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.system.mixininterface.IEntityRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {

    @Inject(at = @At("HEAD"),
            method = "renderLabelIfPresent",
            cancellable = true)
    private void onRenderLabelIfPresent(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (EventManager.postEvent(EntityLabelRenderEvent.get(((IEntityRenderState)state).helios$getEntity(),state, text, matrices, vertexConsumers, light)).isCanceled()) {
            ci.cancel();
        }
    }
}
