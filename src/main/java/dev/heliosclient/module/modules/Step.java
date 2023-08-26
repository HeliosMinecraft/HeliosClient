package dev.heliosclient.module.modules;

import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.tick.Tick;

import javax.security.auth.callback.TextInputCallback;

public class Step extends Module_
{
    DoubleSetting stepHeight = new DoubleSetting("Height", "Height which step should step up at.",this, 1, 1, 10, 1);
    BooleanSetting shiftSuppress = new BooleanSetting("Crouch suppress", "Disables step when crouch key is pressed.", this, true);

    protected static MinecraftClient mc = MinecraftClient.getInstance();

    public Step()
    {
        super("Step", "Allows you to step up full blocks.", Category.MOVEMENT);
        settings.add(stepHeight);
        settings.add(shiftSuppress);
    }

    @Override
    public void onTick(TickEvent event)
    {
        if(mc.player == null) {return;}
        if (shiftSuppress.value) {
        if (mc.options.sneakKey.isPressed()) {
            mc.player.setStepHeight(0.5f);
        } else
        {
            mc.player.setStepHeight((float)stepHeight.value);
        }
        } else
        {
            mc.player.setStepHeight((float)stepHeight.value);
        }
    }

    @Override
    public void onDisable()
    {
        super.onDisable();
        mc.player.setStepHeight(0.5f);
    }
}
