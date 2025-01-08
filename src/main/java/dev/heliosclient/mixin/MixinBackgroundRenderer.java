package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.NoRender;
import dev.heliosclient.module.modules.render.Xray;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.entity.Entity;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public abstract class MixinBackgroundRenderer {
    @Inject(method = "applyFog", at = @At("TAIL"), cancellable = true)
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType, Vector4f color, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
        if ((NoRender.get().isActive() && NoRender.get().noFog.value) || ModuleManager.get(Xray.class).isActive()) {
            if (fogType == BackgroundRenderer.FogType.FOG_TERRAIN) {
                Fog fog = cir.getReturnValue();
                cir.setReturnValue(new Fog(viewDistance * 4, viewDistance * 4.25F,fog.shape(),color.x,color.y,color.z,color.w));
            }
        }
    }

    @Inject(method = "getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;", at = @At("HEAD"), cancellable = true)
    private static void onGetFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable<Object> info) {
        if (NoRender.get().isActive() && NoRender.get().noBlindness.value) info.setReturnValue(null);
    }
}
