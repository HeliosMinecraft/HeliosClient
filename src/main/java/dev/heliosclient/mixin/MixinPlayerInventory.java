package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.render.FreeLook;
import dev.heliosclient.module.modules.render.Zoom;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class MixinPlayerInventory {
    @Inject(at = @At("HEAD"), method = "scrollInHotbar(D)V", cancellable = true)
    private void shouldScrollHotbar(double scrollAmount, CallbackInfo ci) {
        if ((ModuleManager.get(Zoom.class).isActive() && ModuleManager.get(Zoom.class).scrollZoom.value) || (ModuleManager.get(FreeLook.class).isActive() && ModuleManager.get(FreeLook.class).scrollDistance.value)) {
            ci.cancel();
        }
    }
}