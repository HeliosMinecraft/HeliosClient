package dev.heliosclient.module;

import dev.heliosclient.event.EventManager;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.PlayerMotionEvent;
import dev.heliosclient.event.events.RenderEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.KeyBind;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.ui.ModulesListOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.MovementType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;


public abstract class Module_ implements Listener {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    public String name;
    public String description;
    public Category category;
    public ArrayList<Setting> settings;
    public ArrayList<Setting> quickSettings;
    public boolean settingsOpen = false;



    public BooleanSetting chatFeedback = new BooleanSetting("Enable chat feedback", "Toggles feedback in chat.", this, false);
    public BooleanSetting showInModulesList = new BooleanSetting("Show in Modules List", "If this module should show up in Module List.", this, true);
    public KeyBind keyBind = new KeyBind("Keybind", "Key to toggle this module.", this, 0);
    public BooleanSetting active = new BooleanSetting("Active", "State of this module.", this, false);

    public Module_(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        settings = new ArrayList<>();
        quickSettings = new ArrayList<>();
    }

    public void onEnable() {
        ModulesListOverlay.INSTANCE.update();
        if (chatFeedback.value) {
            assert mc.player != null;
            mc.player.sendMessage(Text.literal("[§4Helios] " + this.name + " was enabled."));
        }
        EventManager.register(this);
    }
    public boolean isActive(){
        return active.value;
    }

    public void onDisable() {
        ModulesListOverlay.INSTANCE.update();
        if (chatFeedback.value) {
            assert mc.player != null;
            mc.player.sendMessage(Text.literal("[§4Helios] " + this.name + " was disabled."));
        }
        EventManager.unregister(this);
    }

    @SubscribeEvent
    public void onMotion(PlayerMotionEvent event) {
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
    }
    @SubscribeEvent
    public void render(RenderEvent event){
    }

    public void toggle() {
        active.value = !active.value;
        if (active.value) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public Integer getKeybind() {
        return keyBind.value;
    }

    public void setKeybind(Integer keycode) {
        keyBind.value = keycode;
    }

    public void onLoad() {
        settings.add(showInModulesList);
        settings.add(chatFeedback);
        settings.add(keyBind);
        settings.add(active);
    }

    public void onSettingChange(Setting setting) {}
}
