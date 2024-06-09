package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.client.OpenScreenEvent;
import dev.heliosclient.mixin.AccessorSignEditScreen;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.StringListSetting;
import dev.heliosclient.util.InputBox;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

public class AutoSign extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    StringListSetting lines = sgGeneral.add(new StringListSetting.Builder()
            .name("Text for each line (only 4)")
            .description("The text for each line represented by each text box. Only first four will be counted")
            .defaultBoxes(4)
            .defaultValue(new String[]{"", "", "", "", ""})
            .characterLimit(15)
            .inputMode(InputBox.InputMode.ALL)
            .build()
    );


    public AutoSign() {
        super("AutoSign", "Automatically writes signs for you", Categories.WORLD);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof AbstractSignEditScreen screen) {
            SignBlockEntity sign = ((AccessorSignEditScreen) screen).getSignEntity();

            mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), true, lines.value[0], lines.value[1], lines.value[2], lines.value[3]));
            event.setCanceled(true);
        }
    }
}
