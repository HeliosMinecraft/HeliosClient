package dev.heliosclient.module.modules.combat;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.managers.FriendManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.world.Teams;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.lists.EntityTypeListSetting;
import dev.heliosclient.util.SortMethod;
import dev.heliosclient.util.player.PlayerUtils;
import dev.heliosclient.util.player.TargetUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

public class TriggerBot extends Module_ {

    SettingGroup sgGeneral = new SettingGroup("General");

    DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
            .name("Range")
            .description("Range of the entity to be in order to start attacking")
            .onSettingChange(this)
            .defaultValue(3.5d)
            .range(0, 5d)
            .roundingPlace(1)
            .build()
    );
    DoubleSetting attackDelay = sgGeneral.add(new DoubleSetting.Builder()
            .name("Attack Delay")
            .description("Delay (in ticks) to attack enemy.")
            .onSettingChange(this)
            .range(0, 60d)
            .roundingPlace(0)
            .defaultValue(10d)
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
    EntityTypeListSetting entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("Entities to attack")
            .description("Attacks the selected entities")
            .entities(EntityType.PLAYER, EntityType.ZOMBIE)
            .build()
    );

    TargetUtils targetFinder = new TargetUtils();

    private int delay;
    private LivingEntity targetEntity;

    public TriggerBot() {
        super("TriggerBot", "Attacks entities when you look at them", Categories.COMBAT);
        addSettingGroup(sgGeneral);
        targetFinder.setSortMethod(SortMethod.LowestDistance);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        delay = 0;
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


    @SubscribeEvent
    public void onTickPlayer(TickEvent.PLAYER event) {
        if(pauseInGUI.value && mc.currentScreen != null){
            return;
        }
        if (delay > 0) {
            delay--;
        } else {
            targetEntity = (LivingEntity) targetFinder.getNewTargetIfNull(true);

            if (targetEntity != null) {
                mc.interactionManager.attackEntity(mc.player, targetEntity);
                mc.player.swingHand(Hand.MAIN_HAND);
                delay = (int) attackDelay.value;
            }
        }
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);
        targetFinder.setRange(range.value);
        targetFinder.setFilter(entity ->
                        entity instanceof LivingEntity e &&
                        entities.getSelectedEntries().contains(e.getType()) &&
                        PlayerUtils.isPlayerLookingAtEntity(mc.player, entity, range.value) &&
                        entity.isAlive() &&
                        entity.isAttackable() &&
                        !isTeamMate(e) &&
                        !isFriend(e) &&
                        !isInvisible(e)
        );
    }
}
