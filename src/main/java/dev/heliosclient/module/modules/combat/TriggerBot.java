package dev.heliosclient.module.modules.combat;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.managers.FriendManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.world.AntiBot;
import dev.heliosclient.module.modules.world.Teams;
import dev.heliosclient.module.settings.*;
import dev.heliosclient.module.settings.lists.EntityTypeListSetting;
import dev.heliosclient.system.TickRate;
import dev.heliosclient.util.misc.SortMethod;
import dev.heliosclient.util.timer.TickTimer;
import dev.heliosclient.util.entity.TargetUtils;
import dev.heliosclient.util.player.PlayerUtils;
import dev.heliosclient.util.render.TargetRenderer;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

import java.util.List;

public class TriggerBot extends Module_ {

    SettingGroup sgGeneral = new SettingGroup("General");
    SettingGroup sgRender = new SettingGroup("Render");

    DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
            .name("Range")
            .description("Range of the entity to be in order to start attacking")
            .onSettingChange(this)
            .defaultValue(3.5d)
            .range(0, 10d)
            .roundingPlace(1)
            .build()
    );
    BooleanSetting smartDelay = sgGeneral.add(new BooleanSetting.Builder()
            .name("Smart Delay")
            .description("Applies smart delay for different items like swords, axes, ")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    DoubleSetting attackDelay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Attack Delay")
            .description("Delay (in ticks) to attack enemy.")
            .onSettingChange(this)
            .range(0, 60d)
            .roundingPlace(0)
            .defaultValue(10d)
            .shouldRender(() -> !smartDelay.value)
            .build()
    );
    BooleanSetting onlyWithWeapon = sgGeneral.add(new BooleanSetting.Builder()
            .name("Only with weapon")
            .description("Attacks only when you are holding a weapon")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );

    BooleanSetting ignoreTeammate = sgGeneral.add(new BooleanSetting.Builder()
            .name("Ignore teammate")
            .description("Uses teams module to avoid hitting at team members")
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
    BooleanSetting ignoreInvisible = sgGeneral.add(new BooleanSetting.Builder()
            .name("Ignore Invisible")
            .description("Does not attack invisibles.")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    BooleanSetting pauseInGUI = sgGeneral.add(new BooleanSetting.Builder()
            .name("Pause in gui")
            .description("Does not attack when you are in a GUI.")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    BooleanSetting tpsSync = sgGeneral.add(new BooleanSetting.Builder()
            .name("Tps Sync")
            .description("Tries to sync your hits with the tps")
            .onSettingChange(this)
            .defaultValue(true)
            .value(true)
            .build()
    );
    EntityTypeListSetting entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("Entities to attack")
            .description("Attacks the selected entities")
            .entities(EntityType.PLAYER, EntityType.ZOMBIE)
            .build()
    );

    GradientSetting color = sgRender.add(new GradientSetting.Builder()
            .name("Color")
            .description("Color of target rendering")
            .defaultValue("Primary")
            .onSettingChange(this)
            .build()
    );
    DropDownSetting renderMode = sgRender.add(new DropDownSetting.Builder()
            .name("Render Mode")
            .description("Mode of target rendering")
            .onSettingChange(this)
            .defaultValue(List.of(TargetRenderer.RenderMode.values()))
            .defaultListOption(TargetRenderer.RenderMode.Circle)
            .build()
    );
    DoubleSetting radius = sgRender.add(new DoubleSetting.Builder()
            .name("Radius")
            .description("Radius of the circle")
            .onSettingChange(this)
            .range(0, 2d)
            .roundingPlace(1)
            .defaultValue(0.7)
            .shouldRender(() -> renderMode.getOption() == TargetRenderer.RenderMode.Circle)
            .build()
    );

    CycleSetting direction = sgRender.add(new CycleSetting.Builder()
            .name("Gradient Direction")
            .description("Direction of the gradient to apply")
            .onSettingChange(this)
            .defaultValue(List.of(QuadColor.CardinalDirection.values()))
            .defaultListOption(QuadColor.CardinalDirection.EAST)
            .build()
    );
    TargetUtils targetFinder = new TargetUtils();

    private TickTimer timer = new TickTimer();
    private Entity targetEntity;

    public TriggerBot() {
        super("TriggerBot", "Attacks entities when you look at them", Categories.COMBAT);
        addSettingGroup(sgGeneral);
        addSettingGroup(sgRender);

        addQuickSettings(sgGeneral.getSettings());

        targetFinder.setSortMethod(SortMethod.LowestDistance);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        timer = new TickTimer();
    }

    private boolean isTeamMate(LivingEntity targetEntity) {
        return ignoreTeammate.value && ModuleManager.get(Teams.class).isInMyTeam(targetEntity);
    }

    private boolean isFriend(LivingEntity entity) {
        if (!ignoreFriend.value) {
            return false;
        }

        return FriendManager.isFriend(entity.getName().getString());
    }

    private boolean isInvisible(LivingEntity entity) {
        if (!ignoreInvisible.value) {
            return false;
        }

        return entity.isInvisible();

    }

    public boolean shouldAttack() {
        float baseTimer = (!smartDelay.value) ? attackDelay.getInt() : 0.5f;
        if (tpsSync.get()) baseTimer = baseTimer / (TickRate.INSTANCE.getTPS() / 20);

        if (smartDelay.value) {
            return mc.player.getAttackCooldownProgress(baseTimer) >= 1;
        } else {
            return baseTimer == 0 || timer.incrementAndEvery((int) baseTimer);
        }
    }


    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGH)
    public void onTickPlayer(TickEvent.PLAYER event) {
        if(onlyWithWeapon.value && !PlayerUtils.hasWeaponInHand(mc.player)) return;

        if (pauseInGUI.value && mc.currentScreen != null) {
            return;
        }

        if(shouldAttack()) {
            targetEntity = targetFinder.getNewTargetIfNull(true);

            if (targetEntity != null) {
                mc.interactionManager.attackEntity(mc.player, targetEntity);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    @SubscribeEvent
    public void render3d(Render3DEvent event) {
        if (targetEntity == null || !(targetEntity instanceof LivingEntity livingEntity)) return;

        TargetRenderer.INSTANCE.set(livingEntity);
        TargetRenderer.INSTANCE.color = color.get();
        TargetRenderer.INSTANCE.renderMode = (TargetRenderer.RenderMode) renderMode.getOption();
        TargetRenderer.INSTANCE.radius = (float) radius.value;
        TargetRenderer.INSTANCE.dir = (QuadColor.CardinalDirection) direction.getOption();

        TargetRenderer.INSTANCE.render();
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);


        //TODO: Use ray-casting instead of this unholy finding.
        targetFinder.setRange(range.value);
        targetFinder.setFilter(entity -> {

            //Apply to all entities (like end crystal, TNT, armor-stands etc.)
            boolean A = entities.getSelectedEntries().contains(entity.getType()) &&
                    PlayerUtils.isPlayerLookingAtEntity(mc.player, entity, range.value) &&
                    entity.isAlive() &&
                    entity.isAttackable();

            boolean B = true;

            //Apply to only living entities
            if (entity instanceof LivingEntity e) {
                B = !isTeamMate(e) &&
                        !isFriend(e) &&
                        !isInvisible(e) &&
                        !AntiBot.isBot(e);
            }

            return A && B;
        });
    }

}
