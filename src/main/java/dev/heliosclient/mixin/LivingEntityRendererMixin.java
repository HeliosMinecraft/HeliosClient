package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.ESP;
import dev.heliosclient.module.modules.render.NameTags;
import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.heliosclient.util.render.Renderer3D.mc;
import static org.lwjgl.opengl.GL11C.*;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void renderPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        ESP esp = ModuleManager.get(ESP.class);

        //Meteor client
        if (esp.isActive() && esp.throughWalls.value && !esp.isBlackListed(livingEntity)) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(1.0f, -1100000.0f);
        }
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    private void renderPost(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        ESP esp = ModuleManager.get(ESP.class);

        //Meteor client
        if (esp.isActive() && esp.throughWalls.value && !esp.isBlackListed(livingEntity)) {
            glPolygonOffset(1.0f, 1100000.0f);
            glDisable(GL_POLYGON_OFFSET_FILL);
        }
    }

    //Always renders entity labels.
    @Inject(at = @At("HEAD"), method = "hasLabel*", cancellable = true)
    private void onHasLabel(T entity, CallbackInfoReturnable<Boolean> cir) {
        NameTags nt = ModuleManager.get(NameTags.class);
        boolean isMobEntity = nt.mobs.value && entity instanceof MobEntity;
        if (nt.isActive() && (nt.renderSelf.value || isMobEntity)) {
            cir.setReturnValue(true);
        }
    }

    //ClientSide rotation preview in 3rd person
    //From Meteor mixin

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 2, at = @At(value = "STORE", ordinal = 0))
    public float changeYaw(float prevValue, LivingEntity entity) {
        if (entity.equals(mc.player) && RotationUtils.timerSinceLastRotation.getElapsedTicks() < 10) return RotationUtils.serverYaw;
        return prevValue;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 3, at = @At(value = "STORE", ordinal = 0))
    public float changeHeadYaw(float prevValue, LivingEntity entity) {
        if (entity.equals(mc.player) && RotationUtils.timerSinceLastRotation.getElapsedTicks() < 10) return RotationUtils.serverYaw;
        return prevValue;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 5, at = @At(value = "STORE", ordinal = 3))
    public float changePitch(float prevValue, LivingEntity entity) {
        if (entity.equals(mc.player) && RotationUtils.timerSinceLastRotation.getElapsedTicks() < 10) return RotationUtils.serverPitch;
        return prevValue;
    }

}

