package dev.heliosclient.module.modules;

import dev.heliosclient.managers.EventManager;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.util.ISimpleOption;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.ArrayList;
import java.util.List;

public class Fullbright extends Module_ {
    ArrayList<String> modes = new ArrayList<String>(List.of("Gamma", "Night Vision"));
    CycleSetting mode = new CycleSetting("Mode", "Fullbright mode to apply", this, modes, 0);
    DoubleSetting gamma = new DoubleSetting("Gamma", "Desired gamma value", this, 15, 0, 15, 0) {
        @Override
        public boolean shouldRender() {
            return mode.value == 0;
        }
    };

    public Fullbright() {
        super("Fullbright", "Allows you to see in the dark.", Category.RENDER);
        EventManager.register(this);
        settings.add(mode);
        settings.add(gamma);
        quickSettings.add(mode);
        quickSettings.add(gamma);
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
