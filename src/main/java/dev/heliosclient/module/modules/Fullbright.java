package dev.heliosclient.module.modules;

import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.util.ISimpleOption;

public class Fullbright extends Module_ 
{
    public Fullbright()
    {
        super("Fullbright", "Allows you to see in the dark.",  Category.RENDER);
    }

    @Override
    public void onTick()
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
