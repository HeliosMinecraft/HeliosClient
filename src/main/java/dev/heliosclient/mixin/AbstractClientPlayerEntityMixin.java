package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.modules.misc.CapeModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

import dev.heliosclient.managers.CapeManager;
import dev.heliosclient.managers.ModuleManager;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
    @Unique
    private static final Identifier SKIN = new Identifier("textures/entity/elytra.png");

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void modifyCapeTexture(CallbackInfoReturnable<SkinTextures> cir) {
        // Check if the player is the current player
        if (this.equals(HeliosClient.MC.player)) {
            SkinTextures original = cir.getReturnValue();
            //Should not happen
            if(original == null) return;

            if (CapeManager.cape != null && CapeModule.get().isActive()) {

                //Set elytraTexture if Elytra-Setting is enabled
                Identifier elytraTexture = original.elytraTexture();
                if(CapeModule.get().elytra.value){
                    elytraTexture = CapeManager.cape;
                }

                if(elytraTexture == null){
                    elytraTexture = SKIN;
                }

                //Modify the skin texture
                SkinTextures modified = new SkinTextures(original.texture(), original.textureUrl(), CapeManager.cape, elytraTexture, original.model(), original.secure());
                cir.setReturnValue(modified);
            }
        }
    }
}
