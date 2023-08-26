package dev.heliosclient.module.modules;

import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.util.ISimpleOption;

import java.util.ArrayList;

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
    public void onTick(TickEvent event)
    {
        ((ISimpleOption<Double>)(Object)mc.options.getGamma()).setValueUnrestricted(100.0);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        mc.options.getGamma().setValue(1.0);
    }
}
