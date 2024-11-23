package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PostMovementUpdatePlayerEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.system.mixininterface.IVec3d;
import dev.heliosclient.util.player.PlayerUtils;

import java.util.List;

public class Step extends Module_ {
    private final SettingGroup sgStep = new SettingGroup("Step");
    private final SettingGroup sgReverseStep = new SettingGroup("ReverseStep");

    DoubleSetting stepHeight = sgStep.add(new DoubleSetting.Builder()
            .name("Height")
            .description("Height which step should step up at.")
            .onSettingChange(this)
            .value(1.0)
            .defaultValue(1.0)
            .min(0.0)
            .max(10)
            .roundingPlace(1)
            .build()
    );
    BooleanSetting shiftSuppress = sgStep.add(new BooleanSetting.Builder()
            .name("Crouch suppress")
            .description("Disables step when crouch key is pressed.")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    BooleanSetting reverseStep = sgReverseStep.add(new BooleanSetting.Builder()
            .name("Reverse Step")
            .description("Allows you to step down faster")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    DoubleSetting reverseStepHeight = sgReverseStep.add(new DoubleSetting.Builder()
            .name("ReverseStep Height")
            .description("Height which reverse step should trigger")
            .onSettingChange(this)
            .value(0.5)
            .defaultValue(0.5)
            .min(0.0)
            .max(10)
            .roundingPlace(1)
            .build()
    );
    CycleSetting reverseStepMode = sgReverseStep.add(new CycleSetting.Builder()
            .name("Reverse Step Mode")
            .onSettingChange(this)
            .defaultValue(List.of(ReverseStepMode.values()))
            .defaultListOption(ReverseStepMode.MOTION)
            .build()
    );
    BooleanSetting fixXZVelocity = sgReverseStep.add(new BooleanSetting.Builder()
            .name("Fix Horizontal Velocity")
            .description("Will set your horizontal velocity to 0 to minimise rubber-banding when shifting")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .shouldRender(()-> reverseStepMode.isOption(ReverseStepMode.SHIFT))
            .build()
    );
    DoubleSetting reverseStepMotion = sgReverseStep.add(new DoubleSetting.Builder()
            .name("Motion Factor")
            .description("Force of pull down")
            .onSettingChange(this)
            .value(0.7)
            .defaultValue(0.7)
            .min(0.0)
            .max(1.5)
            .roundingPlace(1)
            .shouldRender(()-> reverseStepMode.isOption(ReverseStepMode.MOTION))
            .build()
    );
    DoubleSetting shiftTicks = sgReverseStep.add(new DoubleSetting.Builder()
            .name("Shift Ticks")
            .description("How many ticks to simulate and shift")
            .onSettingChange(this)
            .value(3)
            .defaultValue(3)
            .min(1)
            .max(5)
            .roundingPlace(0)
            .shouldRender(()->reverseStepMode.isOption(ReverseStepMode.SHIFT))
            .build()
    );

    private float previousHeight = 0.5f;
    private boolean wasOnGround = false;

    public Step() {
        super("Step", "Allows you to step up full blocks.", Categories.MOVEMENT);

        addSettingGroup(sgStep);
        addSettingGroup(sgReverseStep);

        addQuickSettings(sgStep.getSettings());
        addQuickSettings(sgReverseStep.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null)
            previousHeight = mc.player.getStepHeight();
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (mc.player == null) return;

        wasOnGround = mc.player.isOnGround();

        if (shouldNotStep()) {
            return;
        }

        mc.player.setStepHeight((float) stepHeight.value);

        if (shiftSuppress.value) {
            if (mc.options.sneakKey.isPressed()) {
                mc.player.setStepHeight(0.5f);
            }
        }

        if (reverseStep.value && reverseStepMode.isOption(ReverseStepMode.MOTION) && PlayerUtils.isSpaceBelowEmpty(reverseStepHeight.value)) {
            mc.player.addVelocity(0, -reverseStepMotion.value, 0);
        }
    }
    @SubscribeEvent
    public void onPostMovement(PostMovementUpdatePlayerEvent e){
        if(reverseStep.value && reverseStepMode.isOption(ReverseStepMode.SHIFT)){
            if (shouldNotStep()) {
                return;
            }
            if (mc.player.getVelocity().y < 0 && wasOnGround && PlayerUtils.isSpaceBelowEmpty(reverseStepHeight.value) && !mc.player.isOnGround() ) {
                if(fixXZVelocity.value) ((IVec3d) mc.player.getVelocity()).heliosClient$setXZ(0, 0);
                e.setNumberOfTicks(shiftTicks.getInt());
                e.cancel();
            }
        }
    }

    public boolean shouldNotStep(){
        return mc.player.isInLava()
                || mc.player.isRiding()
                || mc.player.isTouchingWater()
                || mc.player.isFallFlying()
                || mc.player.isHoldingOntoLadder()
                || mc.player.input.jumping
                || mc.player.input.sneaking;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null) {
            return;
        }
        wasOnGround = false;
        mc.player.setStepHeight(previousHeight);
    }

    public enum ReverseStepMode{
        MOTION,
        SHIFT
    }
}
