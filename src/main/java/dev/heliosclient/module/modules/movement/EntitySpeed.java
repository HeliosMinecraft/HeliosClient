package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.entity.EntityMotionEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.system.mixininterface.IVec3d;
import dev.heliosclient.util.player.PlayerUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class EntitySpeed extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    DoubleSetting speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Speed")
            .description("Entity Speed")
            .onSettingChange(this)
            .value(10d)
            .defaultValue(10d)
            .min(0)
            .max(80)
            .roundingPlace(0)
            .build()
    );

    BooleanSetting inWater = sgGeneral.add(new BooleanSetting.Builder()
            .name("In Water")
            .description("Apply entity speed while in water")
            .onSettingChange(this)
            .value(false)
            .build()
    );
    BooleanSetting onGround = sgGeneral.add(new BooleanSetting.Builder()
            .name("Only on ground")
            .description("Only apply entity speed while on ground")
            .onSettingChange(this)
            .value(true)
            .build()
    );

    public EntitySpeed() {
        super("EntitySpeed", "Modifies entity movement speed", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onEntityMove(EntityMotionEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        if (event.getEntity().getControllingPassenger() != mc.player) return;

        if (!inWater.value && entity.isTouchingWater()) return;
        if (onGround.value && !entity.isOnGround()) return;

        //Todo: test
        Vec3d vel = PlayerUtils.getHorizontalVelocity(speed.value);
        ((IVec3d) event.getMovement()).heliosClient$set(vel.x, event.getMovement().y, vel.z);
    }
}
