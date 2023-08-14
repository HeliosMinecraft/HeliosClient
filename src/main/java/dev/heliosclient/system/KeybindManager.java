package dev.heliosclient.system;

import dev.heliosclient.module.ModuleManager;
import dev.heliosclient.module.Module_;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;

public class KeybindManager {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    public static ArrayList<Integer> wasPressed = new ArrayList<>();

    public static void onTick() {
        if (mc == null || mc.player == null) return; // TO BE CHANGED
        if(mc.currentScreen != null)return;
        ArrayList<Integer> isPressed = new ArrayList<>();
        for(Module_ module: ModuleManager.INSTANCE.modules) {
            Integer key = module.getKeybind();
            if (key == 0) {continue;}
            boolean isKeyPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), key);
            if (isPressed.contains(key))
            {
                module.toggle();
                continue;
            }
            if (isKeyPressed) {
                if (!wasPressed.contains(key))
                {
                    isPressed.add(key);
                    module.toggle();
                    wasPressed.add(key);
                }

                } else {
                    wasPressed.remove(Integer.valueOf(key));
                }
        }
    }
}
