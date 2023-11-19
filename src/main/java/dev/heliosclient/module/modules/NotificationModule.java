package dev.heliosclient.module.modules;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class NotificationModule extends Module_ {
    public static NotificationModule INSTANCE = new NotificationModule();
    private final SettingGroup sgNotifications = new SettingGroup("Notifications");
    private final SettingGroup sgConfig = new SettingGroup("Config");
    public BooleanSetting moduleNotification = sgNotifications.add(new BooleanSetting.Builder()
            .name("Module Info")
            .description("Whether to show module state.")
            .module(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    public BooleanSetting playSound = sgConfig.add(new BooleanSetting.Builder()
            .name("Play Sound")
            .description("Whether to play a sound when a notification is made")
            .module(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    public DoubleSetting volume = sgConfig.add(new DoubleSetting.Builder()
            .name("Notification Volume")
            .description("Volume of notification sound")
            .module(this)
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

        addQuickSettingGroup(sgNotifications);
        addQuickSettingGroup(sgConfig);
    }

    @Override
    public boolean isActive() {
        return super.isActive();
    }
}
