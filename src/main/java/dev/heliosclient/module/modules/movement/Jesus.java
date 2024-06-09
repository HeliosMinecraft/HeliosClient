package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.system.mixininterface.IVec3d;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Jesus extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    CycleSetting mode = sgGeneral.add(new CycleSetting.Builder()
            .name("Mode")
            .description("Jesus mode to use")
            .value(List.of("Velocity", "onGround"))
            .defaultListOption("Velocity")
            .addOptionToolTip("Changes velocity to stay above water")
            .addOptionToolTip("Uses onGround flag to stay on water or updates velocity slowly")
            .onSettingChange(this)
            .build()
    );

    public Jesus() {
        super("Jesus", "Allows you to walk on water", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        Entity e = mc.player.getRootVehicle();

        if (e.isSneaking() || e.fallDistance > 3f || mc.player.isSneaking())
            return;

        if (mode.getOption().equals("Velocity")) {
            if ((mc.player.isTouchingWater() || mc.player.isInLava()) && mc.player.getVelocity().y < 0) {
                ((IVec3d) mc.player.getVelocity()).heliosClient$set(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
            }
        } else {
            if (isSubmerged(e.getPos().add(0, 0.3, 0))) {
                e.setVelocity(e.getVelocity().x, 0.08, e.getVelocity().z);
            } else if (isSubmerged(e.getPos().add(0, 0.1, 0))) {
                e.setVelocity(e.getVelocity().x, 0.05, e.getVelocity().z);
            } else if (isSubmerged(e.getPos().add(0, 0.05, 0))) {
                e.setVelocity(e.getVelocity().x, 0.01, e.getVelocity().z);
            } else if (isSubmerged(e.getPos())) {
                e.setVelocity(e.getVelocity().x, -0.005, e.getVelocity().z);
                e.setOnGround(true);
            }
        }
    }

    private boolean isSubmerged(Vec3d pos) {
        BlockPos bp = BlockPos.ofFloored(pos);
        assert mc.world != null;
        FluidState state = mc.world.getFluidState(bp);

        return !state.isEmpty() && pos.getY() - bp.getY() <= state.getHeight();
    }
}
