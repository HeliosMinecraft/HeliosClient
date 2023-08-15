package dev.heliosclient.module.modules;

import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module_ 
{
    DoubleSetting speed = new DoubleSetting("Speed", this, 2, 0.1, 10, 1);
    public Speed() 
    {
        super("Speed", "Allows you to move faster.", Category.MOVEMENT);
    }
    
    @Override
    public void onMotion(MovementType type, Vec3d movement)
    {
        assert mc.player != null;
        mc.player.addVelocity(movement);
    }

}
