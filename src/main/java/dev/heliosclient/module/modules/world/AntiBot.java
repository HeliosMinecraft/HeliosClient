package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.entity.EntityAddedEvent;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Objects;

public class AntiBot extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting noNPC = sgGeneral.add(new BooleanSetting.Builder()
            .name("No NPC")
            .description("Removes actual NPCs")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    BooleanSetting noIllegalName = sgGeneral.add(new BooleanSetting.Builder()
            .name("No Illegal Name")
            .description("Removes entities with illegal name")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting noIllegalPitch = sgGeneral.add(new BooleanSetting.Builder()
            .name("No Illegal Pitch")
            .description("Removes entities with illegal pitch")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting noFakeID = sgGeneral.add(new BooleanSetting.Builder()
            .name("No FakeEntity Id")
            .description("Removes entities with fake id")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting duplicate = sgGeneral.add(new BooleanSetting.Builder()
            .name("No Duplicates")
            .description("Removes Duplicate Entities")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting noGamemode = sgGeneral.add(new BooleanSetting.Builder()
            .name("No Game mode")
            .description("Removes entities with no game mode")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting illegalHealth = sgGeneral.add(new BooleanSetting.Builder()
            .name("No Illegal health")
            .description("Removes entities with illegal health")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting removeFromWorld = sgGeneral.add(new BooleanSetting.Builder()
            .name("Remove from world")
            .description("Removes entities that match the above conditions from the world (WARNING: This will remove them from the world, so you want be able to see them or interact with them)")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );

    public AntiBot() {
        super("Anti Bot","An AntiBot module", Categories.WORLD);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onEntityAdd(EntityAddedEvent event){
        if(removeFromWorld.value || mc.world == null) return;

        if(!(event.entity instanceof LivingEntity livingEntity))return;

        if(isBot(livingEntity)){
            mc.world.removeEntity(livingEntity.getId(), Entity.RemovalReason.DISCARDED);
        }
    }

    public static AntiBot get() {
        return ModuleManager.get(AntiBot.class);
    }

    public static boolean isBot(LivingEntity entity){
        if (!get().isActive()) {
            return false;
        }
        if (get().noIllegalName.value && hasIllegalName(entity)) {
            return true;
        }
        if (get().noFakeID.value && isFakeEntityId(entity)) {
            return true;
        }
        if (get().noIllegalPitch.value && hasIllegalPitch(entity)) {
            return true;
        }

        if(entity instanceof PlayerEntity player) {
            if (get().illegalHealth.value && hasIllegalHealth(player)) {
                return true;
            }

            if (get().duplicate.value && isDuplicate(player)) {
                return true;
            }

            if (get().noGamemode.value && hasNoGamemode(player)) {
                return true;
            }

            if (get().noNPC.value) {
                return mc.getNetworkHandler().getPlayerListEntry(player.getUuid()) == null;
            }
        }

        return false;
    }


    private static boolean hasIllegalName(LivingEntity entity) {
        if(entity.getName() == null) return true;
        String name = entity.getName().getString();
        return name.length() < 3 || name.length() > 16 || !name.matches("^[a-zA-Z0-9_]+$");
    }

    private static boolean hasIllegalPitch(Entity entity) {
        return entity.getPitch(1.0F) > 90.0F;
    }

    private static boolean isFakeEntityId(LivingEntity entity) {
        double entityId = entity.getId();
        return entityId < 1.0 || entityId >= 1E+9;
    }

    private static boolean isDuplicate(PlayerEntity player) {
        return mc.getNetworkHandler().getPlayerList()
                .stream()
                .filter(playerListEntry -> Objects.equals(playerListEntry.getProfile().getName(), player.getName().getString()) && playerListEntry.getProfile().getId() == player.getUuid())
                .count()
                != 1;
    }

    private static boolean hasNoGamemode(PlayerEntity player) {
        return mc.getNetworkHandler().getPlayerListEntry(player.getUuid()) == null;
    }

    private static boolean hasIllegalHealth(PlayerEntity player) {
        return player.getHealth() <= 0.0F || player.getHealth() > 22;
    }
}
