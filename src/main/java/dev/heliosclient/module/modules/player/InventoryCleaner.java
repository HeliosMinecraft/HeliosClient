package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.lists.ItemListSetting;
import dev.heliosclient.util.player.InventoryUtils;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class InventoryCleaner extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    ItemListSetting items = sgGeneral.add(new ItemListSetting.Builder()
            .name("Items")
            .description("The items to drop from inventory")
            .items(Items.STRING, Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.BONE)
            .build()
    );


    public InventoryCleaner() {
        super("InventoryCleaner", "Removes the given items from your inventory to prevent clogging", Categories.PLAYER);

        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        for (Item item : items.getSelectedEntries()) {
            InventoryUtils.dropAllItems(item);
        }
    }
}
