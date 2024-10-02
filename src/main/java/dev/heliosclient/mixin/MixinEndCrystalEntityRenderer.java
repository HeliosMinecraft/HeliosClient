package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.CrystalESP;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.EnderDragonEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static net.minecraft.client.render.entity.EndCrystalEntityRenderer.SINE_45_DEGREES;

@Mixin(value = EndCrystalEntityRenderer.class,priority = 1002)
public abstract class MixinEndCrystalEntityRenderer extends EntityRenderer<EndCrystalEntity> {
    @Unique
    private static final Identifier CLEAR_TEXTURE = new Identifier(HeliosClient.MODID, "splashscreen/clear.png");
    @Shadow
    @Final
    private ModelPart frame;

    @Shadow
    @Final
    private ModelPart core;

    protected MixinEndCrystalEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Shadow
    public static float getYOffset(EndCrystalEntity crystal, float tickDelta) {
        return 0;
    }

    @Shadow @Final public ModelPart bottom;

    @Shadow @Final private static RenderLayer END_CRYSTAL;

    // Bounce
    @ModifyReturnValue(method = "getYOffset", at = @At(value = "RETURN"))
    private static float getYOff(float original) {
        CrystalESP esp = ModuleManager.get(CrystalESP.class);
        if (!esp.isActive()) return original;

        return (float) ((original + 1.4f) * esp.bounce.value - 1.4F + esp.yOffset.value);
    }

    @Inject(method = "render(Lnet/minecraft/entity/decoration/EndCrystalEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",at = @At("HEAD"),cancellable = true)
    public void render(EndCrystalEntity endCrystalEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
        CrystalESP esp = ModuleManager.get(CrystalESP.class);

        matrixStack.push();
        float h = getYOffset(endCrystalEntity, g);
        float j = ((float)endCrystalEntity.endCrystalAge + g) * 3.0F;
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(END_CRYSTAL);

        // Texture rendering
        if (esp.isActive() && !esp.texture.value) {
            vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(CLEAR_TEXTURE));
        }

        matrixStack.push();
        matrixStack.scale(2.0F, 2.0F, 2.0F);
        matrixStack.translate(0.0F, -0.5F, 0.0F);
        int k = OverlayTexture.DEFAULT_UV;
        if (endCrystalEntity.shouldShowBottom()) {
            this.bottom.render(matrixStack, vertexConsumer, i, k);
        }

        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        matrixStack.translate(0.0F, 1.5F + h / 2.0F, 0.0F);
        matrixStack.multiply((new Quaternionf()).setAngleAxis(1.0471976F, SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));

        if (esp.isActive())
            matrixStack.scale((float) esp.scaleOutside.value, (float) esp.scaleOutside.value, (float) esp.scaleOutside.value);

        if (esp.isActive()) {
            if (esp.renderFrameOutSide.value) {
                Color color = esp.frameColor.value;
                frame.render(matrixStack, vertexConsumer, i, k, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            }
        } else {
            this.frame.render(matrixStack, vertexConsumer, i, k);
        }

        if (esp.isActive())
            matrixStack.scale((float) esp.scaleInside.value, (float) esp.scaleInside.value, (float) esp.scaleInside.value);

        matrixStack.scale(0.875F, 0.875F, 0.875F);
        matrixStack.multiply((new Quaternionf()).setAngleAxis(1.0471976F, SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        if (esp.isActive()) {
            if (esp.renderFrameInside.value) {
                Color color = esp.frameColor.value;
                frame.render(matrixStack, vertexConsumer, i, k, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            }
        } else {
            this.frame.render(matrixStack, vertexConsumer, i, k);
        }

        matrixStack.scale(0.875F, 0.875F, 0.875F);
        matrixStack.multiply((new Quaternionf()).setAngleAxis(1.0471976F, SINE_45_DEGREES, 0.0F, SINE_45_DEGREES));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
        if (esp.isActive()) {
            if (esp.renderCore.value) {
                Color color = esp.coreColor.value;
                this.core.render(matrixStack, vertexConsumer, i, k, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            }
        } else {
            this.core.render(matrixStack, vertexConsumer, i, k);
        }
        matrixStack.pop();
        matrixStack.pop();
        BlockPos blockPos = endCrystalEntity.getBeamTarget();
        if (blockPos != null) {
            float m = (float)blockPos.getX() + 0.5F;
            float n = (float)blockPos.getY() + 0.5F;
            float o = (float)blockPos.getZ() + 0.5F;
            float p = (float)((double)m - endCrystalEntity.getX());
            float q = (float)((double)n - endCrystalEntity.getY());
            float r = (float)((double)o - endCrystalEntity.getZ());
            matrixStack.translate(p, q, r);
            EnderDragonEntityRenderer.renderCrystalBeam(-p, -q + h, -r, g, endCrystalEntity.endCrystalAge, matrixStack, vertexConsumerProvider, i);
        }

        super.render(endCrystalEntity, f, g, matrixStack, vertexConsumerProvider, i);
        ci.cancel();
    }

}
