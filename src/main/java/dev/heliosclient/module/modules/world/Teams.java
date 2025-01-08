package dev.heliosclient.module.modules.world;

import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class Teams extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");
    BooleanSetting scoreBoardTeam = sgGeneral.add(new BooleanSetting.Builder()
            .name("ScoreBoard Team")
            .description("Gets player team by the scoreboard data")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    BooleanSetting accountArmorColor = sgGeneral.add(new BooleanSetting.Builder()
            .name("Account leather armor color")
            .description("Sets team according to color of their armor pieces. Useful when servers don't use normal minecraft Team.")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );
    DoubleSetting armorIndex = sgGeneral.add(new DoubleSetting.Builder()
            .name("Armor index")
            .description("The index of the armor piece (0 - 3) to use to identify the player's team ")
            .onSettingChange(this)
            .defaultValue(0d)
            .range(0, 3)
            .roundingPlace(0)
            .shouldRender(() -> accountArmorColor.value)
            .build()
    );

    public Teams() {
        super("Teams", "Manages and configs certain modules to account as friends team members", Categories.WORLD);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());

    }

    public boolean isInMyTeam(LivingEntity livingEntity) {
        if (livingEntity == null || !isActive()) {
            return false;
        }

        if (scoreBoardTeam.value && mc.player.getScoreboardTeam() != null && livingEntity.getScoreboardTeam() != null
                && (mc.player.isTeamPlayer(livingEntity.getScoreboardTeam()) || mc.player.getScoreboardTeam().getPlayerList().contains(livingEntity.getName().getString()))) {
            return true;
        }

        if (livingEntity instanceof PlayerEntity player) {
            ItemStack playerArmorPiece = mc.player.getInventory().armor.get((int) armorIndex.value);
            ItemStack entityArmorPiece = player.getInventory().armor.get((int) armorIndex.value);

            if (accountArmorColor.value && isArmorDyeable(playerArmorPiece) && isArmorDyeable(entityArmorPiece)) {
                return getDyeColor(playerArmorPiece) == getDyeColor(entityArmorPiece);
            }
        }
        return false;
    }
    public boolean isArmorDyeable(ItemStack stack) {
        return stack != null && stack.getItem().getComponents().contains(DataComponentTypes.DYED_COLOR);
    }
    public int getDyeColor(ItemStack stack) {
        return stack.getItem().getComponents().get(DataComponentTypes.DYED_COLOR).rgb();
    }

    public int getActualTeamColor(LivingEntity livingEntity) {
        if (livingEntity == null) {
            return -1;
        }

        if (scoreBoardTeam.value && livingEntity.getScoreboardTeam() != null && livingEntity.getScoreboardTeam().getColor() != null) {
            return livingEntity.getScoreboardTeam().getColor().getColorValue() == null ? -1 :livingEntity.getScoreboardTeam().getColor().getColorValue();
        }

        if (livingEntity instanceof PlayerEntity player) {
            ItemStack entityArmorPiece = player.getInventory().armor.get((int) armorIndex.value);

            if (accountArmorColor.value && isArmorDyeable(entityArmorPiece)) {
                return getDyeColor(entityArmorPiece);
            }
        }
        return -1;
    }
}
