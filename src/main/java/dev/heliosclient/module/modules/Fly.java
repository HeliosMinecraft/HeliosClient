package dev.heliosclient.module.modules;

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

        mc.player.getAbilities().flying = false;
    }
}
