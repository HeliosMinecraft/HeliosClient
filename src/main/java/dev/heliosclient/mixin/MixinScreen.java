package dev.heliosclient.mixin;

import dev.heliosclient.event.events.client.ScreenResizeEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MixinScreen{

    @Shadow public int width;

    @Shadow public int height;

    @Inject(method = "resize", at = @At(value = "HEAD"))
    private void onResize(MinecraftClient client, int width, int height, CallbackInfo ci) {
        EventManager.postEvent(new ScreenResizeEvent(this.width, this.height,width,height));
    }
}
