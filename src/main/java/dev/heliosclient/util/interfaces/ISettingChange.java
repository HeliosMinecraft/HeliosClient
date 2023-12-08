package dev.heliosclient.util.interfaces;

import dev.heliosclient.module.settings.Setting;

public interface ISettingChange {
    void onSettingChange(Setting setting);
}
