package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ModuleManager;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrashReport.class)
public abstract class MixinCrashReport {

    //Save our config when a crash occurs via the create method.
    @Inject(method = "create", at = @At("HEAD"))
    private static void onCreateCrashReport(Throwable cause, String title, CallbackInfoReturnable<CrashReport> cir) {
        CrashReport report = cir.getReturnValue();


        //Display details of HeliosClient
        if(report != null) {
            if(HeliosClient.CONFIG != null && HeliosClient.CONFIG.nullCheck()) {
                HeliosClient.saveConfigHook();
            }
        }
    }
}
