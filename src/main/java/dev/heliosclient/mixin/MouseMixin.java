package dev.heliosclient.mixin;

import dev.heliosclient.ui.clickgui.ClickGUIScreen;
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

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (client.currentScreen instanceof ClickGUIScreen)
            ClickGUIScreen.onScroll(horizontal, vertical);

    }
}
