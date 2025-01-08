package dev.heliosclient.mixin;

import com.google.common.base.MoreObjects;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.render.ArmRenderEvent;
import dev.heliosclient.event.events.render.HeldItemRendererEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.ViewModel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.heliosclient.util.render.Renderer3D.mc;

/**
 * Credits: Meteor Client
 */
@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {

    @Shadow
    private float equipProgressMainHand;

    @Shadow
    private float equipProgressOffHand;

    @Shadow
    private ItemStack mainHand;

    @Shadow
    private ItemStack offHand;

    // Swing modification
    @ModifyVariable(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "STORE", ordinal = 0), index = 6)
    private float modifySwing(float swingProgress) {
        ViewModel viewModel = ModuleManager.get(ViewModel.class);
        if (viewModel == null) return swingProgress;
        Hand hand = MoreObjects.firstNonNull(HeliosClient.MC.player.preferredHand, Hand.MAIN_HAND);

        if (viewModel.isActive()) {
            if (hand == Hand.MAIN_HAND && !HeliosClient.MC.player.getMainHandStack().isEmpty()) {
                return (float) (swingProgress + viewModel.mainHandprog.value);
            }
            if (hand == Hand.OFF_HAND && !HeliosClient.MC.player.getOffHandStack().isEmpty()) {
                return (float) (swingProgress + viewModel.offHandprog.value);
            }
        }

        return swingProgress;
    }

    // Old Animations
    @ModifyArg(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal = 2), index = 0)
    private float modifyEquipProgressMainhand(float value) {
        float f = mc.player.getAttackCooldownProgress(1f);
        float modified = ModuleManager.get(ViewModel.class).oldAnimations() ? 1 : f * f * f;

        return (ItemStack.areEqual(mainHand, mc.player.getMainHandStack()) ? modified : 0) - equipProgressMainHand;
    }

    @ModifyArg(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", ordinal = 3), index = 0)
    private float modifyEquipProgressOffhand(float value) {
        return (ItemStack.areEqual(offHand, mc.player.getOffHandStack()) ? 1 : 0) - equipProgressOffHand;
    }

    //Held item rendering
    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void onRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        EventManager.postEvent(new HeldItemRendererEvent(hand, matrices));
    }

    // Arm rendering
    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderArmHoldingItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IFFLnet/minecraft/util/Arm;)V"))
    private void onRenderArm(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        EventManager.postEvent(new ArmRenderEvent(hand, matrices));
    }

    // food
    @Inject(method = "applyEatOrDrinkTransformation", at = @At(value = "INVOKE", target = "Ljava/lang/Math;pow(DD)D", shift = At.Shift.BEFORE), cancellable = true)
    private void cancelTransformations(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        if (ModuleManager.get(ViewModel.class) != null && ModuleManager.get(ViewModel.class).isActive()) {
            if (ModuleManager.get(ViewModel.class).disableFoodAnimation.value) ci.cancel();
        }
    }
}
