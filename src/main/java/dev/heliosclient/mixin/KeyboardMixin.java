package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.events.KeyPressedEvent;
import dev.heliosclient.ui.clickgui.ClickGUIScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info) {
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT && !(HeliosClient.MC.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof AbstractInventoryScreen)
                && !(mc.currentScreen instanceof GameMenuScreen)) {
            ClickGUIScreen.INSTANCE.onLoad();
            MinecraftClient.getInstance().setScreen(ClickGUIScreen.INSTANCE);
        }
        KeyPressedEvent event = new KeyPressedEvent(window, key, scancode, action, modifiers);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }
}
