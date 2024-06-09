package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.EntityUtils;
import dev.heliosclient.util.player.InventoryUtils;
import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoNametag extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");


    DoubleSetting range = sgGeneral.add(new DoubleSetting.Builder()
            .name("Range")
            .description("Maximum range of the entity, to be nametagged")
            .onSettingChange(this)
            .defaultValue(3.5d)
            .range(0, 5d)
            .roundingPlace(1)
            .build()
    );

    BooleanSetting rotate = sgGeneral.add(new BooleanSetting.Builder()
            .name("Rotate")
            .description("Rotate to look at the mob being name tagged")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting renametag = sgGeneral.add(new BooleanSetting.Builder()
            .name("Re-Nametag")
            .description("Renames entity who already have been nametagged")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );

    Entity nameTagEntity;

    public AutoNametag() {
        super("AutoNameTag", "Automatically nametags nearby entities", Categories.WORLD);

        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        int slot = InventoryUtils.findItemInHotbar(Items.NAME_TAG);

        if (slot == -1) {
            ChatUtils.sendHeliosMsg("NameTag not found in hotbar. Disabling...");
            toggle();
            return;
        }

        nameTagEntity = EntityUtils.getNearestEntity(mc.world, mc.player, range.value, entity -> {
            if (entity.hasCustomName()) {
                return renametag.value && !entity.getCustomName().equals(mc.player.getInventory().getStack(slot).getName());
            }
            return true;
        });

        if (nameTagEntity == null) {
            ChatUtils.sendHeliosMsg("No nearby entity found within range, toggling off");
            toggle();
            return;
        }

        InventoryUtils.swapToSlot(slot, true);
        // Interaction
        if (rotate.get()) {
            RotationUtils.lookAt(nameTagEntity, RotationUtils.LookAtPos.CENTER);
            interact(slot == 45);
        } else {
            interact(slot == 45);
        }
    }

    private void interact(boolean offHand) {
        mc.interactionManager.interactEntity(mc.player, nameTagEntity, offHand ? Hand.OFF_HAND : Hand.MAIN_HAND);
        InventoryUtils.swapBackHotbar();
    }

}
