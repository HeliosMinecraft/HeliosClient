package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.CrystalESP;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(EndCrystalEntityRenderer.class)
public abstract class MixinEndCrystalEntityRenderer {
    @Unique
    private static final Identifier CLEAR_TEXTURE = new Identifier(HeliosClient.MODID, "splashscreen/clear.png");
    @Shadow
    @Final
    private ModelPart frame;
    @Shadow
    @Final
    private ModelPart core;

    // Bounce
    @ModifyReturnValue(method = "getYOffset", at = @At(value = "RETURN"))
    private static float getYOff(float original) {
        CrystalESP esp = ModuleManager.get(CrystalESP.class);
        if (!esp.isActive()) return original;

        return (float) ((original + 1.4f) * esp.bounce.value - 1.4F + esp.yOffset.value);
    }

    // Texture rendering
    @Redirect(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;"))
    private VertexConsumer redirectRenderLayer(VertexConsumerProvider instance, RenderLayer renderLayer) {
        CrystalESP esp = ModuleManager.get(CrystalESP.class);
        if (esp.isActive() && !esp.texture.value) {
            return instance.getBuffer(RenderLayer.getEntityTranslucent(CLEAR_TEXTURE));
        } else {
            return instance.getBuffer(renderLayer);
        }
    }

    // Core
    @Redirect(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal = 3))
    private void redirectCoreRendering(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        CrystalESP esp = ModuleManager.get(CrystalESP.class);
        if (esp.isActive()) {
            if (esp.renderCore.value) {
                Color color = esp.coreColor.value;
                core.render(matrices, vertices, light, overlay, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            }
        } else {
            core.render(matrices, vertices, light, overlay);
        }
    }

    // Frames
    @Redirect(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal = 1))
    private void renderFrameOutside(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        CrystalESP esp = ModuleManager.get(CrystalESP.class);
        if (esp.isActive()) {
            if (esp.renderFrameOutSide.value) {
                Color color = esp.frameColor.value;
                frame.render(matrices, vertices, light, overlay, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            }
        } else {
            frame.render(matrices, vertices, light, overlay);
        }
    }

    @Redirect(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V", ordinal = 2))
    private void renderFrameInside(ModelPart modelPart, MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        CrystalESP esp = ModuleManager.get(CrystalESP.class);
        if (esp.isActive()) {
            if (esp.renderFrameInside.value) {
                Color color = esp.frameColor.value;
                frame.render(matrices, vertices, light, overlay, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            }
        } else {
            frame.render(matrices, vertices, light, overlay);
        }
    }

    // Scaling
    @Inject(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", ordinal = 1))
    public void insideScaling(EndCrystalEntity endCrystalEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        CrystalESP esp = ModuleManager.get(CrystalESP.class);
        if (esp.isActive())
            matrixStack.scale((float) esp.scaleInside.value, (float) esp.scaleInside.value, (float) esp.scaleInside.value);
    }

    @Inject(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V", ordinal = 0))
    public void outsideScaling(EndCrystalEntity endCrystalEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        CrystalESP esp = ModuleManager.get(CrystalESP.class);
        if (esp.isActive())
            matrixStack.scale((float) esp.scaleOutside.value, (float) esp.scaleOutside.value, (float) esp.scaleOutside.value);
    }


}
