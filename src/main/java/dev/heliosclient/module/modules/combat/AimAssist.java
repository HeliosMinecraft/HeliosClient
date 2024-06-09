package dev.heliosclient.module.modules.combat;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.world.Teams;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.DropDownSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.EntityUtils;
import dev.heliosclient.util.player.RotationSimulator;
import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public class AimAssist extends Module_ {
    RotationSimulator simulator = new RotationSimulator();

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
    DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
            .name("Range")
            .description("Range of the entity")
            .onSettingChange(this)
            .value(4d)
            .defaultValue(3d)
            .min(0.0)
            .max(5d)
            .roundingPlace(1)
            .build()
    );
    DropDownSetting lookAt = sgGeneral.add(new DropDownSetting.Builder()
            .name("LookAt priority")
            .description("What part of the body of the entity to look at")
            .onSettingChange(this)
            .value(List.of(RotationUtils.LookAtPos.values()))
            .defaultListOption(RotationUtils.LookAtPos.CENTER)
            .build()
    );
    BooleanSetting pauseInGUI = sgGeneral.add(new BooleanSetting.Builder()
            .name("Pause In GUI")
            .description("Pauses rotation when you are in GUI")
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
    BooleanSetting simulateRotation = sgGeneral.add(new BooleanSetting.Builder()
            .name("Simulate Rotation")
            .description("Simulates your rotation to the enemy instead of blatantly moving")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    DoubleSetting simulateTime = sgGeneral.add(new DoubleSetting.Builder()
            .name("SimulateTime")
            .description("Time to simulate the rotation in ticks")
            .onSettingChange(this)
            .value(60d)
            .defaultValue(60d)
            .min(0.0)
            .max(240)
            .roundingPlace(0)
            .shouldRender(() -> simulateRotation.value)
            .build()
    );
    DoubleSetting randomness = sgGeneral.add(new DoubleSetting.Builder()
            .name("Randomness")
            .description("Adds random degrees of movement as per the value set ( in degrees). 1.0 == 1 degree of randomness")
            .onSettingChange(this)
            .value(1d)
            .defaultValue(1d)
            .min(0)
            .max(90)
            .roundingPlace(0)
            .shouldRender(() -> simulateRotation.value)
            .build()
    );

    BooleanSetting players = sgEntities.add(new BooleanSetting("Players", "Aim at players", this, true, () -> true, true));
    BooleanSetting passive = sgEntities.add(new BooleanSetting("Passive", "Aim at passives", this, false, () -> true, true));
    BooleanSetting hostiles = sgEntities.add(new BooleanSetting("Hostiles", "Aim at hostiles", this, false, () -> true, true));
    BooleanSetting items = sgEntities.add(new BooleanSetting("Items", "Aim at items", this, false, () -> true, true));
    BooleanSetting tamed = sgEntities.add(new BooleanSetting("Tamed", "Aim at tamed", this, false, () -> true, true));
    BooleanSetting others = sgEntities.add(new BooleanSetting("Other", "Aim at others", this, false, () -> true, true));

    public AimAssist() {
        super("AimAssist", "Helps you aim better to your enemy (currently meele only)", Categories.COMBAT);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgEntities);

        addQuickSettings(sgGeneral.getSettings());
        addQuickSettings(sgEntities.getSettings());
    }

    private static int sortAngle(Entity e1, Entity e2) {
        boolean e1l = e1 instanceof LivingEntity;
        boolean e2l = e2 instanceof LivingEntity;

        if (!e1l && !e2l) return 0;
        else if (e1l && !e2l) return 1;
        else if (!e1l) return -1;

        double e1yaw = Math.abs(RotationUtils.getYaw(e1.getPos()) - mc.player.getYaw());
        double e2yaw = Math.abs(RotationUtils.getYaw(e2.getPos()) - mc.player.getYaw());

        double e1pitch = Math.abs(RotationUtils.getPitch(e1.getPos()) - mc.player.getPitch());
        double e2pitch = Math.abs(RotationUtils.getPitch(e2.getPos()) - mc.player.getPitch());

        return Double.compare(e1yaw * e1yaw + e1pitch * e1pitch, e2yaw * e2yaw + e2pitch * e2pitch);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        EventManager.register(simulator);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        EventManager.unregister(simulator);
        simulator.clearRotations();
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        RotationSimulator.pauseInGUI = pauseInGUI.value;
        Entity entity = EntityUtils.getNearestEntity(
                mc.world,
                mc.player, range.value,
                entity1 -> entity1 instanceof LivingEntity && !isBlackListed(entity1) && entity1.distanceTo(mc.player) < range.value,
                (entity0, entity1) -> {
                    return switch ((SortMethod) sort.getOption()) {
                        case LowestDistance ->
                                Float.compare(mc.player.distanceTo(entity0), mc.player.distanceTo(entity1));
                        case FarthestDistance ->
                                Float.compare(mc.player.distanceTo(entity1), mc.player.distanceTo(entity0));
                        case LowestHealth ->
                                Float.compare(((LivingEntity) entity0).getHealth(), ((LivingEntity) entity1).getHealth());
                        case HighestHealth ->
                                Float.compare(((LivingEntity) entity1).getHealth(), ((LivingEntity) entity0).getHealth());
                        case LowestAngle -> sortAngle(entity0, entity1);
                    };
                });

        if (ignoreTeammate.value && entity instanceof LivingEntity entity1 && ModuleManager.get(Teams.class).isInMyTeam(entity1)) {
            return;
        }

        if (entity != null) {
            if (simulateRotation.value) {
                RotationSimulator.INSTANCE.simulateRotation(entity, false, null, (int) simulateTime.value,(int)randomness.value, (RotationUtils.LookAtPos) lookAt.getOption());
            } else {
                RotationUtils.lookAt(entity, (RotationUtils.LookAtPos) lookAt.getOption());
            }
        } else {
            simulator.clearRotations();
        }
    }

    public boolean isBlackListed(Entity entity) {
        return (!(entity instanceof HostileEntity) || !hostiles.value) &&
                (!(entity instanceof PassiveEntity) || !passive.value) &&
                (!(entity instanceof TameableEntity) || !tamed.value) &&
                (!(entity instanceof PlayerEntity) || !players.value) &&
                (!(entity instanceof ItemEntity) || !items.value) &&
                !others.value;
    }

    public enum SortMethod {
        LowestDistance,
        FarthestDistance,
        LowestHealth,
        HighestHealth,
        LowestAngle,
    }
}
