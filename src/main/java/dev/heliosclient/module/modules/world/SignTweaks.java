package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.client.OpenScreenEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.mixin.AccessorSignEditScreen;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.StringListSetting;
import dev.heliosclient.util.inputbox.InputBox;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SignTweaks extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");
    SettingGroup sgAutoFill = new SettingGroup("AutoFill");

    BooleanSetting autoDateAndTime = sgGeneral.add(new BooleanSetting.Builder()
            .name("Auto Date and Time")
            .description("Automatically puts current date and time in the last line of the sign. Note: Text for the last line will be ignored.")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );

    BooleanSetting autoFill = sgAutoFill.add(new BooleanSetting.Builder()
            .name("Auto Fill")
            .description("Automatically fills all the lines of the sign for you.")
            .onSettingChange(this)
            .defaultValue(true)
            .build()
    );

    StringListSetting lines = sgAutoFill.add(new StringListSetting.Builder()
            .name("Text for each line (only 4)")
            .description("The text for each line represented by each text box. Only first four will be counted")
            .defaultBoxes(4)
            .defaultValue(new String[]{"HeliosClient on Top!", "", "", "", ""})
            .characterLimit(15)
            .inputMode(InputBox.InputMode.ALL)
            .shouldRender(()->autoFill.value)
            .build()
    );


    public SignTweaks() {
        super("SignTweaks", "Automatically writes signs for you", Categories.WORLD);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
        addSettingGroup(sgAutoFill);
        addQuickSettings(sgAutoFill.getSettings());
    }

    //Non-US
    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mm a");

    @SubscribeEvent
    public void onPacketSend(PacketEvent.SEND event){
        if(autoDateAndTime.value &&  event.packet instanceof UpdateSignC2SPacket p){
            p.getText()[3] = dateFormat.format(new Date()).toLowerCase();
        }
    }

    @SubscribeEvent
    public void onOpenScreen(OpenScreenEvent event) {
        if (autoFill.value && event.screen instanceof AbstractSignEditScreen screen) {
            SignBlockEntity sign = ((AccessorSignEditScreen) screen).getSignEntity();

            mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), true, lines.value[0], lines.value[1], lines.value[2], lines.value[3]));
            event.setCanceled(true);
        }
    }
}
