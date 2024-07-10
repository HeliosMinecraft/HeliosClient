package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.mixin.AccessorKeybind;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.combat.AimAssist;
import dev.heliosclient.module.modules.world.Teams;
import dev.heliosclient.module.modules.world.Timer;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.SortMethod;
import dev.heliosclient.util.player.PlayerUtils;
import dev.heliosclient.util.player.RotationUtils;
import dev.heliosclient.util.player.TargetUtils;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.List;

//IDK why I added this
public class TargetStrafe extends Module_ {

    static boolean strafeDirectionChanged = false;
    SettingGroup sgGeneral = new SettingGroup("General");
    SettingGroup sgEntities = new SettingGroup("Entities");
    DropDownSetting sort = sgGeneral.add(new DropDownSetting.Builder()
            .name("Sort")
            .description("Sort entities on basis of.")
            .onSettingChange(this)
            .value(List.of(SortMethod.values()))
            .defaultListOption(SortMethod.LowestHealth)
            .build()
    );
    BooleanSetting legit = sgGeneral.add(new BooleanSetting.Builder()
            .name("Legit")
            .description("Uses movement keys to simulate strafing. Should work on most servers but does not offer proper void checks. Use aim assist for better rotation bypass")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    DoubleSetting speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Speed")
            .description("Speed of the movement")
            .onSettingChange(this)
            .defaultValue(2d)
            .min(0.0)
            .max(12d)
            .roundingPlace(1)
            .shouldRender(() -> !legit.value)
            .build()
    );
    DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
            .name("Range")
            .description("Range of the entity")
            .onSettingChange(this)
            .defaultValue(4d)
            .min(2.5)
            .max(10d)
            .roundingPlace(1)
            .build()
    );
    DoubleSetting timer = sgGeneral.add(new DoubleSetting.Builder()
            .name("Timer")
            .description("Uses timer to speed up movement. 1 for default no extra")
            .onSettingChange(this)
            .defaultValue(1d)
            .min(1)
            .max(10d)
            .roundingPlace(2)
            .build()
    );
    BooleanSetting canSeeEntity = sgGeneral.add(new BooleanSetting.Builder()
            .name("Raycast check")
            .description("Checks if the entity is visible to the player or not")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    BooleanSetting ignoreTeammate = sgGeneral.add(new BooleanSetting.Builder()
            .name("Ignore teammate")
            .description("Uses teams module to avoid aiming at team members")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    BooleanSetting jump = sgGeneral.add(new BooleanSetting.Builder()
            .name("Jump")
            .description("Jumps while moving")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    BooleanSetting players = sgEntities.add(new BooleanSetting("Players", "Aim at players", this, true, () -> true, true));
    BooleanSetting passive = sgEntities.add(new BooleanSetting("Passive", "Aim at passives", this, false, () -> true, true));
    BooleanSetting hostiles = sgEntities.add(new BooleanSetting("Hostiles", "Aim at hostiles", this, false, () -> true, true));
    BooleanSetting items = sgEntities.add(new BooleanSetting("Items", "Aim at items", this, false, () -> true, true));
    BooleanSetting tamed = sgEntities.add(new BooleanSetting("Tamed", "Aim at tamed", this, false, () -> true, true));
    BooleanSetting others = sgEntities.add(new BooleanSetting("Other", "Aim at others", this, false, () -> true, true));


    public TargetStrafe() {
        super("TargetStrafe", "Strafe around a target", Categories.MOVEMENT);

        addSettingGroup(sgGeneral);
        addSettingGroup(sgEntities);

    }

    // check if the player can strafe to the new position
    private static boolean canStrafeTo(int playerYPos, int newX, int newZ) {
        BlockPos pos = new BlockPos(newX, playerYPos, newZ).down(2);

        // check for blocks in the way
        // check for empty space in the way
        return !mc.world.isAir(pos) || !mc.player.horizontalCollision;
    }

    private boolean isEntityVisible(Entity entity) {
        if (!canSeeEntity.value) {
            return true;
        }

        return PlayerUtils.canSeeEntity(entity);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        setPressedNoDirectKey(mc.options.forwardKey, false);
        setPressedNoDirectKey(mc.options.leftKey, false);
        setPressedNoDirectKey(mc.options.rightKey, false);

        if (timer.value != 1.0) {
            ModuleManager.get(Timer.class).setOverride(Timer.RESET);
        }
    }

    @SubscribeEvent
    public void onTicks(TickEvent.PLAYER event) {
        TargetUtils.getInstance().setRange(range.value);
        LivingEntity entity = (LivingEntity) TargetUtils.getInstance().getNewTargetIfNull(e-> !isBlackListed(e),true);

        if (ignoreTeammate.value && ModuleManager.get(Teams.class).isInMyTeam(entity)) {
            return;
        }

        if (entity != null && entity.distanceTo(mc.player) <= range.value) {
            if (mc.player == null || !entity.isAlive()) return;

            Vec3d targetPos = entity.getPos();
            Vec3d playerPos = mc.player.getPos();

            if (timer.value != 1.0) {
                ModuleManager.get(Timer.class).setOverride(timer.value);
            }


            if (legit.value) {
                //Look at target only if AimAssist is not enabled
                //Major issue with this would be desync of target entities between AimAssist and target strafe.
                //Could be fixed by having a TargetUtils class to handle targets which should keep them both in sync
                //and every other module.
                if (!ModuleManager.get(AimAssist.class).isActive()) {
                    RotationUtils.lookAt(entity.getBoundingBox().getCenter());
                }


                //Move forward if player is not nearby to the entity enough
                if (mc.player.distanceTo(entity) < range.value) {
                    setPressedNoDirectKey(mc.options.forwardKey, true);
                }


                if (strafeDirectionChanged) {
                    setPressedNoDirectKey(mc.options.rightKey, true);
                } else {
                    setPressedNoDirectKey(mc.options.leftKey, true);
                }

                //Todo: Fix impl. currently not working / failing
                if (!canStrafeTo(mc.player.getBlockPos().getY(), mc.player.getBlockX(), mc.player.getBlockZ()) || mc.player.horizontalCollision) {
                    if (!strafeDirectionChanged) {
                        strafeDirectionChanged = true;
                    }
                } else {
                    strafeDirectionChanged = false;
                }

            } else {
                doMotionStrafe(playerPos, entity, targetPos);
            }
            // jump while strafing
            if (jump.value && mc.player.isOnGround()) {
                mc.player.jump();
            }
        } else if (legit.value) {
            setPressedNoDirectKey(mc.options.forwardKey, mc.options.forwardKey.isPressed());
            setPressedNoDirectKey(mc.options.leftKey, mc.options.leftKey.isPressed());
            setPressedNoDirectKey(mc.options.rightKey, mc.options.rightKey.isPressed());
        }
    }

    public void setPressedNoDirectKey(KeyBinding bind, boolean pressed) {
        int code = ((AccessorKeybind) bind).getKey().getCode();
        mc.keyboard.onKey(mc.getWindow().getHandle(), code, GLFW.glfwGetKeyScancode(code), pressed ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE, 0);
        if (pressed)
            mc.keyboard.onKey(mc.getWindow().getHandle(), code, GLFW.glfwGetKeyScancode(code), GLFW.GLFW_REPEAT, 0);
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);

        if (mc.options != null) {
            setPressedNoDirectKey(mc.options.forwardKey, false);
            setPressedNoDirectKey(mc.options.leftKey, false);
            setPressedNoDirectKey(mc.options.rightKey, false);
        }
    }

    public void doMotionStrafe(Vec3d playerPos, Entity entity, Vec3d targetPos) {
        double dx = targetPos.x - playerPos.x;
        double dz = targetPos.z - playerPos.z;

        double angle = MathHelper.atan2(dz, dx);

        // calculate the strafe movement
        double strafeX = Math.cos(angle + Math.PI / 2) * speed.value * 0.05; // reduced movement multiplier
        double strafeZ = Math.sin(angle + Math.PI / 2) * speed.value * 0.05;

        // normalize the movement vector to prevent excessive movement
        double length = Math.sqrt(strafeX * strafeX + strafeZ * strafeZ);
        strafeX /= length;
        strafeZ /= length;

        if (strafeDirectionChanged) {
            angle += Math.PI;
        }

        if (!canStrafeTo(mc.player.getBlockPos().getY(), (int) strafeX, (int) strafeZ)) {
            if (!strafeDirectionChanged) {
                angle += Math.PI;
                strafeX = Math.cos(angle + Math.PI / 2) * speed.value * 0.05;
                strafeZ = Math.sin(angle + Math.PI / 2) * speed.value * 0.05;
                strafeDirectionChanged = true;
            }
        } else {
            strafeDirectionChanged = false; // Reset the flag if strafing is successful
        }

        // update the player's rotation to face the target
        RotationUtils.lookAt(entity.getEyePos());
        mc.player.bodyYaw = mc.player.getYaw(mc.getTickDelta());
        mc.player.headYaw = mc.player.getYaw(mc.getTickDelta());


        // add a small forward movement to keep the player moving towards the target
        strafeX += Math.cos(angle) * 0.15;
        strafeZ += Math.sin(angle) * 0.15;


        // set the player's horizontal movement
        mc.player.setVelocity(new Vec3d(strafeX, mc.player.getVelocity().y, strafeZ));
    }

    public boolean isBlackListed(Entity entity) {
        return (!(entity instanceof HostileEntity) || !hostiles.value) &&
                (!(entity instanceof PassiveEntity) || !passive.value) &&
                (!(entity instanceof TameableEntity) || !tamed.value) &&
                (!(entity instanceof PlayerEntity) || !players.value) &&
                (!(entity instanceof ItemEntity) || !items.value) &&
                !others.value;
    }
}
