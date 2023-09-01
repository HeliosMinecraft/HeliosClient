package dev.heliosclient.mixin;

import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.sysmodules.ClickGUI;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import dev.heliosclient.ui.clickgui.ClientSettingsScreen;
import dev.heliosclient.ui.clickgui.SettingsScreen;
import dev.heliosclient.util.animation.Easing;
import dev.heliosclient.util.animation.EasingType;
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
        if (client.currentScreen instanceof SettingsScreen)
        SettingsScreen.onScroll(horizontal, vertical);
        else if(client.currentScreen instanceof ClickGUIScreen)
        ClickGUIScreen.onScroll(horizontal, vertical);
        else if (client.currentScreen instanceof ClientSettingsScreen)
        ClientSettingsScreen.onScroll(horizontal, vertical);
    }
}
