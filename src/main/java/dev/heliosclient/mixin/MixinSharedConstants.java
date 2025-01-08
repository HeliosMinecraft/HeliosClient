package dev.heliosclient.mixin;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SharedConstants.class)
public abstract class MixinSharedConstants {

    /*
    @ModifyReturnValue(method = "", at = @At("RETURN"))
    private static boolean isValidChar(boolean original) {
        if (ModuleManager.get(ChatTweaks.class).noKeyRestriction()) return true;
        return original;
    }
    
     */
}
