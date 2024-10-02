package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.Fullbright;
import dev.heliosclient.module.modules.render.Xray;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {

    @ModifyVariable(method = "update", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    private float modifyGamma(float originalGamma) {
        if (ModuleManager.get(Fullbright.class).isActive()) {
            return ModuleManager.get(Fullbright.class).getGamma(originalGamma);
        }

        if (ModuleManager.get(Xray.class).isActive()) {
            return 5.0f;
        }

        return originalGamma;
    }
}

