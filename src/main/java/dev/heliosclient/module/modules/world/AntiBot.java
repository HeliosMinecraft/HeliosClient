package dev.heliosclient.module.modules.world;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Category;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
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

    public AntiBot() {
        super("Anti Bot","An Antibot module", Categories.WORLD);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    public static AntiBot get(){
        return ModuleManager.get(AntiBot.class);
    }

    public static boolean isBot(LivingEntity entity){
        if(entity instanceof PlayerEntity player) {
            if (!get().isActive()) {
                return false;
            }

            if (get().illegalHealth.value && hasIllegalHealth(player)) {
                return true;
            }

            if (get().duplicate.value && isDuplicate(player)) {
                return true;
            }

            if (get().noIllegalName.value && isIllegalName(player)) {
                return true;
            }

            if (get().noFakeID.value && isFakeEntityId(player)) {
                return true;
            }

            if (get().noGamemode.value && hasNoGamemode(player)) {
                return true;
            }

            if (get().noIllegalPitch.value && isIllegalPitch(player)) {
                return true;
            }

            if (get().noNPC.value) {
                return mc.getNetworkHandler().getPlayerListEntry(player.getUuid()) == null;
            }
        }

        return false;
    }


    private static boolean isIllegalName(PlayerEntity player) {
        String name = player.getName().getString();
        return name.length() < 3 || name.length() > 16 || !name.matches("^[a-zA-Z0-9_]+$");
    }

    private static boolean isIllegalPitch(PlayerEntity player) {
        return player.getPitch(1.0F) > 90.0F;
    }

    private static boolean isFakeEntityId(PlayerEntity player) {
        double entityId = player.getId();
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
