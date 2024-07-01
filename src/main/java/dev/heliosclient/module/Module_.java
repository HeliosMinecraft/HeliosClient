package dev.heliosclient.module;

import com.moandjiezana.toml.Toml;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PlayerMotionEvent;
import dev.heliosclient.event.events.render.RenderEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.managers.NotificationManager;
import dev.heliosclient.module.modules.misc.NotificationModule;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.KeyBind;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.ui.notification.notifications.InfoNotification;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.SoundUtils;
import dev.heliosclient.util.interfaces.ISaveAndLoad;
import dev.heliosclient.util.interfaces.ISettingChange;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.MinecraftClient;

import java.util.*;

/**
 * Template for modules.
 */
public abstract class Module_ implements Listener, ISettingChange, ISaveAndLoad {
    protected static MinecraftClient mc = MinecraftClient.getInstance();
    public String name;
    public String description;
    public Category category;
    public Set<SettingGroup> settingGroups;
    public Set<Setting<?>> quickSettings;
    public boolean settingsOpen = false;
    public SettingGroup sgbind = new SettingGroup("Bind");

    /**
     * Setting indicating if chat feedback for this module should be shown. Don't remove, that will cause crash.
     */
    public BooleanSetting chatFeedback = sgbind.add(new BooleanSetting.Builder()
            .name("Enable chat feedback")
            .description("Toggles feedback in chat.")
            .onSettingChange(this)
            .value(false)
            .build()
    );

    /**
     * Setting that will tell module list if it should be shown. Don't remove, that will cause crash.
     */
    public BooleanSetting showInModulesList = sgbind.add(new BooleanSetting.Builder()
            .name("Show in Modules List")
            .description("If this module should show up in Module List.")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    /**
     * Key-bind setting. Don't remove, that will cause crash.
     */
    public KeyBind keyBind = sgbind.add(new KeyBind.Builder()
            .name("Keybind")
            .description("Key to toggle this module.")
            .onSettingChange(this)
            .value(-1)
            .defaultValue(-1)
            .build()
    );
    /**
     * Key-bind setting. Don't remove, that will cause crash.
     */
    public BooleanSetting toggleOnBindRelease = sgbind.add(new BooleanSetting.Builder()
            .name("Toggle On Bind Release")
            .description("Toggle on if key is being held and off if key is released")
            .onSettingChange(this)
            .value(false)
            .defaultValue(false)
            .build()
    );

    /**
     * Value indicating if module is enabled. Don't remove, that will cause crash.
     */
    public BooleanSetting active = sgbind.add(new BooleanSetting.Builder()
            .name("Active")
            .description("State of this module.")
            .onSettingChange(this)
            .value(false)
            .build()
    );

    public Module_(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        settingGroups = new ObjectArraySet<>();
        quickSettings = new ObjectArraySet<>();
    }

    /**
     * Returns a string containing necessary details for the module.
     * Override in your module to return the data you need to.
     */
    public String getInfoString(){
        return "";
    }


    /**
     * Adds a setting group to the main {@link #settingGroups} list
     *
     * @param settingGroup settingGroup to be added
     */
    public void addSettingGroup(SettingGroup settingGroup) {
        this.settingGroups.add(settingGroup);
    }

    /**
     * Adds a setting to the {@link #quickSettings} list
     *
     * @param setting setting to be added
     */
    public void addQuickSetting(Setting<?> setting) {
        this.quickSettings.add(setting);
    }

    /**
     * Adds all the settings from the list to the {@link #quickSettings} list
     * <p>
     * Intended to be used with {@link SettingGroup#getSettings()}
     * </p>
     *
     * @param setting setting list to be added
     */
    public void addQuickSettings(List<Setting<?>> setting) {
        this.quickSettings.addAll(setting);
    }

    /**
     * Called on enable. Probably shouldn't disable original functionality since it will remove chat feedback.
     */
    public void onEnable() {
        active.value = true;
        if (chatFeedback.value) {
            assert mc.player != null;
            ChatUtils.sendHeliosMsg(this.name + " was enabled.");
        }
        EventManager.register(this);
    }

    /**
     * @return Active status.
     */
    public boolean isActive() {
        return active.value;
    }

    /**
     * Called on disable. Probably shouldn't override without super call since it will remove chat feedback.
     */
    public void onDisable() {
        active.value = false;
        if (chatFeedback.value) {
            assert mc.player != null;
            ChatUtils.sendHeliosMsg(this.name + " was disabled.");
        }
        EventManager.unregister(this);
    }

    /**
     * Called on player motion.
     */
    @SubscribeEvent
    public void onMotion(PlayerMotionEvent event) {
    }

    /**
     * Called on tick.
     */
    @SubscribeEvent
    public void onTick(TickEvent event) {
    }

    /**
     * Called on render.
     */
    @SubscribeEvent
    public void render(RenderEvent event) {
    }

    /**
     * Toggles the module.
     */
    public void toggle() {
        active.value = !active.value;
        if (active.value) {
            onEnable();
            sendNotification(true);
        } else {
            onDisable();
            sendNotification(false);
        }
    }

    public void sendNotification(boolean enabled) {
        String description = enabled ? "was enabled!" : "was disabled!";

        if (ModuleManager.get(NotificationModule.class).moduleNotification.value && HeliosClient.shouldSendNotification()) {
            NotificationManager.addNotification(new InfoNotification(this.name, description, 2000, SoundUtils.TING_SOUNDEVENT, enabled ? 1f : 0.5f));
        }
    }

    /**
     * @return Key that is this module bound to.
     */
    public Integer getKeybind() {
        return keyBind.value;
    }

    /**
     * Sets key-bind to a module.
     *
     * @param keycode Target key-code represented by an GLFW integer.
     */
    public void setKeybind(Integer keycode) {
        keyBind.value = keycode;
    }

    /**
     * Called on load. Override to remove default settings.
     */
    public void onLoad() {
        addSettingGroup(sgbind);
    }

    public String getNameWithInfo() {
        return name + "[" + getInfoString() + "]";
    }

    /**
     * Called when setting is changed.
     *
     * @param setting Setting that got changed.
     */
    public void onSettingChange(Setting<?> setting) {
        if (setting == active) {
            if (active.value) {
                onEnable();
                sendNotification(true);
            } else {
                onDisable();
                sendNotification(false);
            }
        }
    }

    @Override
    public Object saveToToml(List<Object> list) {
        Map<String, Object> ModuleConfig = new HashMap<>();
        // Map for storing the values of each module
        if (this.settingGroups == null) return ModuleConfig;

        for (SettingGroup settingGroup : this.settingGroups) {
            for (Setting<?> setting : settingGroup.getSettings()) {
                if (!setting.shouldSaveAndLoad()) continue;

                if (setting.name != null) {
                    // Put the value of each setting into the map. Call the setting saveToToml method to get the value of the setting.
                    ModuleConfig.put(setting.name.replace(" ", ""), setting.saveToToml(new ArrayList<>()));
                }
            }
        }
        return ModuleConfig;
    }

    @Override
    public void loadFromToml(Map<String, Object> MAP, Toml toml) {
        for (SettingGroup settingGroup : this.settingGroups) {
            for (Setting<?> setting : settingGroup.getSettings()) {
                if (!setting.shouldSaveAndLoad()) break;


                Toml settingTable = toml.getTable(this.name.replace(" ", ""));
                if (settingTable != null) {

                    //Any error caught should not cause the whole config system to fail to load.

                    try {
                        setting.loadFromToml(settingTable.toMap(), settingTable);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if (setting == this.active && this.isActive()) {
                    this.onEnable();
                }
            }
        }
    }
}
