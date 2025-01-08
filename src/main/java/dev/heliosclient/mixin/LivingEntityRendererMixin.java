package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.ESP;
import dev.heliosclient.module.modules.render.NameTags;
import dev.heliosclient.system.mixininterface.IEntityRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.lwjgl.opengl.GL11C.*;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void renderPre(S state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        ESP esp = ModuleManager.get(ESP.class);

        //Meteor client
        if (esp.isActive() && esp.throughWalls.value && !esp.isBlackListed(IEntityRenderState.class.cast(state).helios$getEntity())) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(1.0f, -1100000.0f);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    private void renderPost(S state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        ESP esp = ModuleManager.get(ESP.class);

        //Meteor client
        if (esp.isActive() && esp.throughWalls.value && !esp.isBlackListed(IEntityRenderState.class.cast(state).helios$getEntity())) {
            glPolygonOffset(1.0f, 1100000.0f);
            glDisable(GL_POLYGON_OFFSET_FILL);
        }
    }

    //Always renders entity labels.
    @Inject(at = @At("HEAD"), method = "hasLabel(Lnet/minecraft/entity/Entity;D)Z", cancellable = true)
    private void onHasLabel(Entity entity, double squaredDistanceToCamera, CallbackInfoReturnable<Boolean> cir) {
        NameTags nt = ModuleManager.get(NameTags.class);
        boolean isMobEntity = nt.mobs.value && entity instanceof MobEntity;
        if (nt.isActive() && (nt.renderSelf.value || isMobEntity)) {
            cir.setReturnValue(true);
        }
    }
}

