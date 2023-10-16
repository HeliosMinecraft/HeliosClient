package dev.heliosclient.module.modules;

import dev.heliosclient.managers.EventManager;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingBuilder;
import dev.heliosclient.util.ISimpleOption;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.ArrayList;
import java.util.List;

public class Fullbright extends Module_ {
    private final SettingBuilder sgGeneral = new SettingBuilder("General");

    ArrayList<String> modes = new ArrayList<String>(List.of("Gamma", "Night Vision"));
    CycleSetting mode = sgGeneral.add(new CycleSetting.Builder()
            .name("Mode")
            .description("Fullbright mode to apply")
            .module(this)
            .value(modes)
            .listValue(0)
            .build()
    );
    DoubleSetting gamma = sgGeneral.add(new DoubleSetting.Builder()
            .name("Gamma")
            .description("Desired gamma value")
            .module(this)
            .value(15.0)
            .min(0)
            .max(15)
            .roundingPlace(0)
            .build());


    public Fullbright() {
        super("Fullbright", "Allows you to see in the dark.", Category.RENDER);
        EventManager.register(this);

        gamma.setVisibilityCondition(() -> mode.value == 0);

        settingBuilders.add(sgGeneral);

        quickSettingsBuilder.add(sgGeneral);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onSettingChange(Setting setting) {
        super.onSettingChange(setting);
        if (active.value) {
            if (mode.value == 0) {
                ((ISimpleOption<Double>) (Object) mc.options.getGamma()).setValueUnrestricted(gamma.value);
                assert mc.player != null;
                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            } else if (mc.player != null && mode.value == 1) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1000, 254, true, false, false));
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.options.getGamma().setValue(1.0);
        if (mc.player != null)
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
    }
}
