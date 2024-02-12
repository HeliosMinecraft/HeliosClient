package dev.heliosclient.module.modules.misc;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class NotificationModule extends Module_ {
    private final SettingGroup sgNotifications = new SettingGroup("Notifications");
    private final SettingGroup sgConfig = new SettingGroup("Config");
    public BooleanSetting moduleNotification = sgNotifications.add(new BooleanSetting.Builder()
            .name("Module Info")
            .description("Whether to show module state.")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting clientNotification = sgNotifications.add(new BooleanSetting.Builder()
            .name("Client Info")
            .description("Whether to show any messages or info of the client as a notification.")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting playSound = sgConfig.add(new BooleanSetting.Builder()
            .name("Play Sound")
            .description("Whether to play a sound when a notification is made")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    public DoubleSetting volume = sgConfig.add(new DoubleSetting.Builder()
            .name("Notification Volume")
            .description("Volume of notification sound")
            .onSettingChange(this)
            .value(100.0D)
            .defaultValue(100D)
            .min(0D)
            .max(100D)
            .roundingPlace(0)
            .shouldRender(() -> playSound.value)
            .build()
    );

    public NotificationModule() {
        super("Notifications", "Change notifications for certain events", Categories.MISC);
        addSettingGroup(sgNotifications);
        addSettingGroup(sgConfig);

        addQuickSettings(sgNotifications.getSettings());
        addQuickSettings(sgConfig.getSettings());
    }

    @Override
    public boolean isActive() {
        return super.isActive();
    }
}
