package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.input.KeyboardInputEvent;
import dev.heliosclient.managers.FriendManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.world.Teams;
import dev.heliosclient.module.modules.world.Timer;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.util.entity.TargetUtils;
import dev.heliosclient.util.misc.SortMethod;
import dev.heliosclient.util.player.PlayerUtils;
import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class TargetStrafe extends Module_ {
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
            .description("Speed of strafing movement")
            .onSettingChange(this)
            .defaultValue(1)
            .value(1)
            .min(0.0)
            .max(3)
            .roundingPlace(2)
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
    BooleanSetting ignoreInvisible = sgGeneral.add(new BooleanSetting.Builder()
            .name("Ignore Invisible")
            .description("Does not attack invisibles.")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    BooleanSetting ignoreFriend = sgGeneral.add(new BooleanSetting.Builder()
            .name("Ignore Friend")
            .description("Does not attack friends.")
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
    BooleanSetting onlyAttackAttackable = sgGeneral.add(new BooleanSetting.Builder()
            .name("Only attack attackable")
            .description("Only attacks attackable entities")
            .onSettingChange(this)
            .defaultValue(false)
            .value(false)
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
    BooleanSetting tamed = sgEntities.add(new BooleanSetting("Tamed", "Aim at tamed", this, false, () -> true, true));
    BooleanSetting others = sgEntities.add(new BooleanSetting("Other", "Aim at others", this, false, () -> true, true));

    boolean strafeDirectionChanged = false;

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
        if (!canSeeEntity.value || !ignoreInvisible.value) {
            return true;
        }

        return PlayerUtils.canSeeEntity(entity) || entity.isInvisible();
    }

    private boolean isAttackable(Entity entity) {
        if (!onlyAttackAttackable.value) {
            return true;
        }

        return entity.isAttackable();
    }

    private boolean isFriend(Entity entity) {
        if (!ignoreFriend.value || !(entity instanceof PlayerEntity)) {
            return false;
        }

        return FriendManager.isFriend(entity.getName().getString());
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (timer.value != 1.0) {
            ModuleManager.get(Timer.class).setOverride(Timer.RESET);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (timer.value == 1.0) {
            ModuleManager.get(Timer.class).setOverride(Timer.RESET);
        }

        if (legit.value) return;

        TargetUtils.getInstance().sortMethod((SortMethod) sort.getOption());
        TargetUtils.getInstance().setRange(range.value);
        Entity entity = TargetUtils.getInstance().getNewTargetIfNull(e -> !isBlackListed(e) && isEntityVisible(e) && isAttackable(e) && mc.player.distanceTo(e) <= range.value && (onlyAttackAttackable.value && e.isAttackable()) && !isFriend(e), true);

        if (!TargetUtils.getInstance().isLivingEntity() || (ignoreTeammate.value && ModuleManager.get(Teams.class).isInMyTeam((LivingEntity) entity))) {
            return;
        }

        if (entity.distanceTo(mc.player) <= range.value) {
            if (mc.player == null || !entity.isAlive()) return;

            if (timer.value != 1.0) {
                ModuleManager.get(Timer.class).setOverride(timer.value);
            }


            strafeAround(entity);

            // jump while strafing
            if (jump.value && mc.player.isOnGround()) {
                mc.player.jump();
            }
        }
    }

    //Legit mode simulates target-strafe by modifying the input
    @SubscribeEvent
    public void onKeyBoardInput(KeyboardInputEvent event) {
        if(mc.options.leftKey.isPressed()){
            strafeDirectionChanged = true;
        }else if(mc.options.rightKey.isPressed()){
            strafeDirectionChanged = false;
        }

        if (!legit.value || mc.player == null) return;

        TargetUtils.getInstance().sortMethod((SortMethod) sort.getOption());
        TargetUtils.getInstance().setRange(range.value);
        Entity entity = TargetUtils.getInstance().getNewTargetIfNull(e -> !isBlackListed(e) && isEntityVisible(e) && isAttackable(e) && mc.player.distanceTo(e) <= range.value && (onlyAttackAttackable.value && e.isAttackable()), true);

        if (!TargetUtils.getInstance().isLivingEntity() || (ignoreTeammate.value && ModuleManager.get(Teams.class).isInMyTeam((LivingEntity) entity))) {
            return;
        }

            if (timer.value != 1.0) {
                ModuleManager.get(Timer.class).setOverride(timer.value);
            }

            RotationUtils.instaLookAt(entity.getBoundingBox().getCenter());

            //Move forward if player is not nearby to the entity enough
            if (mc.player.distanceTo(entity) > range.value - 1) {
                event.pressingForward = true;
            } else {
                event.pressingBack = true;
            }


            if (strafeDirectionChanged) {
                event.pressingLeft = true;
            } else {
                event.pressingRight = true;
            }

            Vec3d vec = mc.player.getPos().add(mc.player.getVelocity());
            if (PlayerUtils.willFallMoreThanFiveBlocks(vec) || mc.player.horizontalCollision) {
                strafeDirectionChanged = !strafeDirectionChanged;
            }

            if(jump.value){
                event.jumping = true;
            }

            // jump while strafing
            //if (jump.value && !mc.player.isOnGround()) {
              //  mc.player.jump();
            //}
            event.cancel();
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);
    }

    public void strafeAround(Entity target) {
        Vec3d playerPos = mc.player.getPos();
        Vec3d targetPos = target.getPos();
        Vec3d direction = targetPos.subtract(playerPos).normalize();

        double distance = playerPos.distanceTo(targetPos);
        if (distance < range.value - 1) {
            direction = direction.multiply(-1); // Move away from the target
        }

        double strafeAngle = strafeDirectionChanged ? Math.PI / 2 : -Math.PI / 2; // 90 degrees to the right or left
        Vec3d strafeDirection = new Vec3d(
                direction.x * Math.cos(strafeAngle) - direction.z * Math.sin(strafeAngle),
                direction.y,
                direction.x * Math.sin(strafeAngle) + direction.z * Math.cos(strafeAngle)
        ).normalize();

        Vec3d strafePos = playerPos.add(strafeDirection.multiply(speed.value * 0.15f));

        // Check for horizontal collision
        if (PlayerUtils.hasHorizontalCollision(strafePos) || PlayerUtils.willFallMoreThanFiveBlocks(strafePos)) {
            strafeDirectionChanged = !strafeDirectionChanged; // Reverse strafe direction
        } else {
            mc.player.updatePosition(strafePos.x, mc.player.getY(), strafePos.z);
        }

        // Rotate player to face the target
        RotationUtils.instaLookAt(target, RotationUtils.LookAtPos.CENTER);
    }


    //unused
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
                (!(entity instanceof LivingEntity) || !others.value);
    }

    enum StrafeMode{
        Mode_A,
        Mode_B
    }
}
