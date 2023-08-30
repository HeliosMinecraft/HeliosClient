package dev.heliosclient.mixin;

import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.events.KeyPressedEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    private static MinecraftClient mc = MinecraftClient.getInstance();

	@Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info) {
        KeyPressedEvent event = new KeyPressedEvent(window, key, scancode, action, modifiers);
        EventManager.postEvent(event);
        if (event.isCanceled()){
            info.cancel();
        }

        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            if (!(mc.currentScreen instanceof ChatScreen)) {
                ClickGUIScreen.INSTANCE.onLoad();
                mc.setScreen(ClickGUIScreen.INSTANCE);
            }
        }
    }
}
