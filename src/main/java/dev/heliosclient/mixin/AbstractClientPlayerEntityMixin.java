package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.CapeManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.misc.CapeModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
    @Unique
    private static final Identifier SKIN = new Identifier("textures/entity/elytra.png");

    @Unique
    private static @NotNull SkinTextures getModifiedSkinTexture(SkinTextures original) {
        Identifier elytraTexture = original.elytraTexture();
        if (ModuleManager.get(CapeModule.class).elytra.value) {
            elytraTexture = CapeManager.CURRENT_PLAYER_CAPE;
        }

        if (elytraTexture == null) {
            elytraTexture = SKIN;
        }
        //Modify the skin texture
        return new SkinTextures(original.texture(), original.textureUrl(), CapeManager.CURRENT_PLAYER_CAPE, elytraTexture, original.model(), original.secure());
    }

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void modifyCapeTexture(CallbackInfoReturnable<SkinTextures> cir) {
        // Check if the player is the current player
        if (this.equals(HeliosClient.MC.player)) {
            SkinTextures original = cir.getReturnValue();
            //Should not happen
            if (original == null){
                cir.setReturnValue(cir.getReturnValue());
                return;
            }

            if (CapeManager.CURRENT_PLAYER_CAPE != null && ModuleManager.get(CapeModule.class).isActive()) {
                //Set elytraTexture if elytra-setting is enabled
                SkinTextures modified = getModifiedSkinTexture(original);
                cir.setReturnValue(modified);
            }
        }
    }
}
