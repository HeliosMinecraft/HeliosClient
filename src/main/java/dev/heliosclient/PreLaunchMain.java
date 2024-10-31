package dev.heliosclient;

import dev.heliosclient.managers.ColorManager;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class PreLaunchMain implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        ColorManager.createInstance();
    }
}
