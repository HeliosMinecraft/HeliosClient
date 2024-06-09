package dev.heliosclient.module.modules.world;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;

public class Timer extends Module_ {

    public double RESET = 1;
    double override = 1;

    SettingGroup sgGeneral = new SettingGroup("General");
    DoubleSetting timerMultiplier = sgGeneral.add(new DoubleSetting.Builder()
            .name("Timer Multiplier")
            .description("The multipler value to speed the game by")
            .onSettingChange(this)
            .range(0, 50)
            .value(1D)
            .defaultValue(1D)
            .value(1D)
            .roundingPlace(1)
            .build()
    );

    public Timer() {
        super("Timer", "Change the speed of your game", Categories.WORLD);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    public void setOverride(double override) {
        this.override = override;
    }

    public double getTimerMultiplier() {
        return override != RESET ? override : (isActive() ? timerMultiplier.value : RESET);
    }
}
