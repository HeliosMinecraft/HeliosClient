package dev.heliosclient.mixin;

import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.SettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        SettingsScreen.onScroll(horizontal, vertical);
        ClickGUIScreen.onScroll(horizontal, vertical);
    }
}
