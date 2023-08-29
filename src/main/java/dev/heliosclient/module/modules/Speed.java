package dev.heliosclient.module.modules;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.PlayerDamageEvent;
import dev.heliosclient.event.events.PlayerMotionEvent;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module_ 
{
    DoubleSetting speed = new DoubleSetting("Speed", "Multiplier of speed.", this, 2, 0.1, 10, 1);
    public Speed() 
    {
        super("Speed", "Allows you to move faster.", Category.MOVEMENT);
        settings.add(speed);
        quickSettings.add(speed);
    }
    @SubscribeEvent
    public void onMotion(PlayerMotionEvent event)
    {
        assert mc.player != null;
        mc.player.addVelocity(event.getMovement());
    }

}
