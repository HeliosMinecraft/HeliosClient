package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.StringListSetting;
import dev.heliosclient.util.InputBox;
import dev.heliosclient.util.player.InventoryUtils;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class InventoryCleaner extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    StringListSetting items = sgGeneral.add(new StringListSetting.Builder()
            .name("Items")
            .description("The items to drop from inventory")
            .defaultValue(new String[]{"minecraft:rotten_flesh", "minecraft:arrow", "minecraft:spider_eye"})
            .value(new String[]{"minecraft:rotten_flesh", "minecraft:arrow", "minecraft:spider_eye"})
            .characterLimit(100)
            .inputMode(InputBox.InputMode.ALL)
            .defaultBoxes(3)
            .build()
    );


    public InventoryCleaner() {
        super("InventoryCleaner", "Removes the given items from your inventory to prevent clogging", Categories.PLAYER);

        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        for (String itemName : items.value) {
            if (itemName.isEmpty()) continue;
            InventoryUtils.dropAllItems(getItemFromString(itemName));
        }
    }

    private Item getItemFromString(String s) {
        Identifier id = new Identifier(s);
        return Registries.ITEM.get(id);
    }
}
