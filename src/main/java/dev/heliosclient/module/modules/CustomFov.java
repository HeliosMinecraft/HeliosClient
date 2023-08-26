package dev.heliosclient.module.modules;

import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.util.ISimpleOption;

public class CustomFov extends Module_
{
    DoubleSetting FOV = new DoubleSetting("FOV", "Desired FOV.", this, 140, 1, 240, 0);

    private int previousFov = 100;
    public CustomFov()
    {
        super("CustomFOV", "Allows you to set custom field of view.",  Category.RENDER);
        settings.add(FOV);
        quickSettings.add(FOV);
        //quickSettings.add(RainbowPane);
    }

    @Override
    public void onTick(TickEvent event)
    {
        ((ISimpleOption<Integer>)(Object)mc.options.getFov()).setValueUnrestricted((int)(FOV.value));
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
        previousFov = mc.options.getFov().getValue();
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        mc.options.getFov().setValue(previousFov);
    }
}
