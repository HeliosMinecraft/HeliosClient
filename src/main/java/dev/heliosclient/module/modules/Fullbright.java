package dev.heliosclient.module.modules;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.util.ISimpleOption;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.ArrayList;
import java.util.Objects;

public class Fullbright extends Module_ 
{
    ArrayList<String> mode = new ArrayList<>(2);
    CycleSetting setting;
    public Fullbright()
    {
        super("Fullbright", "Allows you to see in the dark.",  Category.RENDER);
        mode.add("Gamma");
        mode.add("Night Vision");
        setting = new CycleSetting("Mode","Fullbright mode to apply",this,mode,0);
        settings.add(setting);
        quickSettings.add(setting);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @SubscribeEvent
    public void onTick(TickEvent event)
    {
        if(Objects.equals(mode.get(setting.value), mode.get(0))) {
            ((ISimpleOption<Double>) (Object) mc.options.getGamma()).setValueUnrestricted(100.0);
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        } else if(mc.player!=null && Objects.equals(mode.get(setting.value), mode.get(1))){
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,1000,254,true,false,false));
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
