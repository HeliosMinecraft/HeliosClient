package dev.heliosclient.managers;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.module.Module_;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;

public class KeybindManager implements Listener {
    public static ArrayList<Integer> wasPressed = new ArrayList<>();
    protected static MinecraftClient mc = MinecraftClient.getInstance();

    public KeybindManager() {
        EventManager.register(this);
    }

    @SubscribeEvent
    public static void onTick(TickEvent event) {
        if (mc == null || mc.player == null) return; // TO BE CHANGED
        if (mc.currentScreen != null) return;
        ArrayList<Integer> isPressed = new ArrayList<>();
        for (Module_ module : ModuleManager.INSTANCE.modules) {
            Integer key = module.getKeybind();
            if (key == 0) {
                continue;
            }
            boolean isKeyPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), key);
            if (isPressed.contains(key)) {
                module.toggle();
                continue;
            }
            if (isKeyPressed) {
                if (!wasPressed.contains(key)) {
                    isPressed.add(key);
                    module.toggle();
                    wasPressed.add(key);
                }

            } else {
                wasPressed.remove(key);
            }
        }
    }
}
