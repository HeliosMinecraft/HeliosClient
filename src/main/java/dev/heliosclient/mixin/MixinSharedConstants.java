package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.chat.ChatTweaks;
import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SharedConstants.class)
public abstract class MixinSharedConstants {

    @ModifyReturnValue(method = "isValidChar", at = @At("RETURN"))
    private static boolean isValidChar(boolean original) {
        if(ModuleManager.get(ChatTweaks.class).noKeyRestriction()) return true;
        return original;
    }
}
