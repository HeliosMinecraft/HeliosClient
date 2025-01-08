package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.CrystalESP;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.model.EndCrystalEntityModel;
import net.minecraft.client.render.entity.state.EndCrystalEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EndCrystalEntityRenderer.class,priority = 1002)
public abstract class MixinEndCrystalEntityRenderer {
    @Unique
    private static final Identifier CLEAR_TEXTURE = Identifier.of(HeliosClient.MODID, "splashscreen/clear.png");

    @Shadow @Final private static RenderLayer END_CRYSTAL;

    @Shadow
    public static float getYOffset(float f) {
        return 0;
    }

    @Shadow @Final private EndCrystalEntityModel model;

    // Bounce
    @ModifyReturnValue(method = "getYOffset", at = @At(value = "RETURN"))
    private static float getYOff(float original) {
        CrystalESP esp = ModuleManager.get(CrystalESP.class);
        if (!esp.isActive()) return original;

        return (float) ((original + 1.4f) * esp.bounce.value - 1.4F + esp.yOffset.value);
    }
    @Redirect(method = "render(Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"))
    public VertexConsumer render(VertexConsumerProvider instance, RenderLayer renderLayer){
        CrystalESP esp = ModuleManager.get(CrystalESP.class);

        // Texture rendering
        if (esp.isActive() && !esp.texture.value) {
            return instance.getBuffer(RenderLayer.getEntityTranslucent(CLEAR_TEXTURE));
        }
        return instance.getBuffer(renderLayer);
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"))
    private void render$scale(EndCrystalEntityRenderState endCrystalEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        CrystalESP esp = ModuleManager.get(CrystalESP.class);

        if (!esp.isActive()) return;

        float scale = esp.scale.getFloat();
        matrixStack.scale(scale, scale, scale);
    }

    //TODO: Modify rendering for frame and core using EndCrystalEntityRenderState
    @Redirect(method = "render(Lnet/minecraft/client/render/entity/state/EndCrystalEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EndCrystalEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
    private void render$modifyColor(EndCrystalEntityModel instance, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        CrystalESP esp = ModuleManager.get(CrystalESP.class);

        int color = -1;

        if (esp.isActive()) {
            color = esp.frameColor.value.getRGB();
        }

        model.render(matrices, vertices, light, overlay, color);
    }
}
