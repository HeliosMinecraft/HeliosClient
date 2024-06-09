package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.movement.TridentTweaker;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TridentItem.class)
public abstract class MixinTridentItem {
    @Redirect(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getRiptide(Lnet/minecraft/item/ItemStack;)I"))
    private int redirectGetRiptide(ItemStack stack) {
        if (ModuleManager.get(TridentTweaker.class).isActive() && ModuleManager.get(TridentTweaker.class).alwaysRiptide.value) {
            return 4;
        } else {
            return EnchantmentHelper.getRiptide(stack);
        }
    }

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isTouchingWaterOrRain()Z"))
    private boolean isInWaterOrRain(boolean original) {
        return (ModuleManager.get(TridentTweaker.class).isActive() && ModuleManager.get(TridentTweaker.class).outOfWater.value) || original;
    }

    @ModifyExpressionValue(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isTouchingWaterOrRain()Z"))
    private boolean isInWaterOrRainPost(boolean original) {
        return (ModuleManager.get(TridentTweaker.class).isActive() && ModuleManager.get(TridentTweaker.class).outOfWater.value) || original;
    }

    @Redirect(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addVelocity(DDD)V"))
    private void redirectAddVelocity(PlayerEntity playerEntity, double x, double y, double z) {
        double modifiedX = x;
        double modifiedY = y;
        double modifiedZ = z;

        if (ModuleManager.get(TridentTweaker.class).isActive() && playerEntity == HeliosClient.MC.player) {
            double speedBoost = ModuleManager.get(TridentTweaker.class).velocityBoost.value;
            modifiedX *= speedBoost;
            modifiedY *= speedBoost;
            modifiedZ *= speedBoost;
        }

        playerEntity.addVelocity(modifiedX, modifiedY, modifiedZ);
    }
}
