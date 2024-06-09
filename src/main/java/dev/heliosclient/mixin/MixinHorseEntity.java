package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.movement.EntityControl;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorseEntity.class)
public abstract class MixinHorseEntity {

    @Inject(method = "isSaddled", at = @At(value = "TAIL"), cancellable = true)
    public void setSaddled(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.get(EntityControl.class).isActive()) cir.setReturnValue(true);
    }
}
