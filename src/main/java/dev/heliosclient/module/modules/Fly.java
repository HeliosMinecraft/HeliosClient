package dev.heliosclient.module.modules;

import com.ibm.icu.impl.Assert;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;

public class Fly extends Module_ 
{
    public Fly() 
    {
        super("Fly", "Allows you to fly in survival mode.", Category.MOVEMENT);
    }
    @Override
    public void onTick()
    {
        mc.player.getAbilities().flying = true;
    }

    
    @Override
    public void onEnable()
    {
        super.onEnable();

    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        if (mc.player != null) {
        mc.player.getAbilities().flying = false;
        }
    }
}
