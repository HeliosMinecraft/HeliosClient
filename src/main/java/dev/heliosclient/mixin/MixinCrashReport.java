package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.modules.render.NoRender;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport {

    @Inject(method = "create", at = @At("HEAD"))
    private static void onCreateCrashReport(Throwable cause, String title, CallbackInfoReturnable<CrashReport> cir) {
        HeliosClient.saveConfigHook();
    }
}
