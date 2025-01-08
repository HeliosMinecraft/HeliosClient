package dev.heliosclient.mixin;

import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.heliosclient.util.render.Renderer3D.mc;

@Mixin(PlayerEntityRenderer.class)
public abstract class MixinPlayerEntityRenderer {

    //ClientSide rotation preview in 3rd person
    //From Meteor mixin
    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("RETURN"))
    private void updateRenderState$rotations(AbstractClientPlayerEntity entity, PlayerEntityRenderState playerEntityRenderState, float f, CallbackInfo ci) {
        if (entity.equals(mc.player) && RotationUtils.timerSinceLastRotation.getElapsedTicks() < 10) {
            playerEntityRenderState.bodyYaw = RotationUtils.serverYaw;
            playerEntityRenderState.pitch = RotationUtils.serverPitch;
        }
    }
}

