package dev.heliosclient.module.modules.misc;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.module.settings.StringSetting;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.inputbox.InputBox;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;


public class BrandSpoof extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    public BooleanSetting spoofBrand = sgGeneral.add(new BooleanSetting.Builder()
            .name("Spoof Brand")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );
    public StringSetting brandToSpoof = sgGeneral.add(new StringSetting.Builder()
            .name("Brand to spoof")
            .description("The brand to spoof")
            .onSettingChange(this)
            .inputMode(InputBox.InputMode.ALL)
            .characterLimit(100)
            .defaultValue("vanilla")
            .shouldRender(() -> spoofBrand.value)
            .build()
    );
    public BooleanSetting spoofResourcePacks = sgGeneral.add(new BooleanSetting.Builder()
            .name("Spoof ResourcePacks")
            .description("Spoof resource packs accepting")
            .onSettingChange(this)
            .value(true)
            .defaultValue(true)
            .build()
    );

    public BrandSpoof() {
        super("BrandSpoof", "Spoofs your client brand (eg. vanilla, forge,etc.) to servers. As well as spoofing resource packs", Categories.MISC);
        addSettingGroup(sgGeneral);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.SEND event) {
        if (!(event.packet instanceof CustomPayloadC2SPacket packet)) return;

        Identifier id = packet.payload().getId().id();

        if (spoofBrand.value && id.equals(BrandCustomPayload.ID.id()) && event.connection != null) {
            try {
                event.connection.send(new CustomPayloadC2SPacket(new BrandCustomPayload(brandToSpoof.value)));
            } catch (Throwable ignored){}
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.RECEIVE event) {
        if (!(event.packet instanceof ResourcePackSendS2CPacket packet)) return;
        if (spoofResourcePacks.value) {
            event.setCanceled(true);

            // Create the base message
            MutableText msg = Text.literal("This server ");

            if (packet.required()) {
                msg.append(Text.literal("requires").formatted(Formatting.BOLD, Formatting.RED))
                        .append(Text.literal(" resource packs"));
            } else {
                msg.append(Text.literal("has optional").formatted(Formatting.ITALIC, Formatting.BLUE))
                        .append(Text.literal(" resource packs"));
            }

            MutableText download = Text.literal("[Download]");
            download.setStyle(download.getStyle()
                    .withColor(Formatting.AQUA)
                    .withUnderline(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, packet.url()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to download!")))
            );

            MutableText spoof = Text.literal("[Spoof Anyways]");
            spoof.setStyle(spoof.getStyle()
                    .withColor(Formatting.DARK_GREEN)
                    .withUnderline(true)
                    .withClickEvent(new RunnableEvent(() -> {
                        mc.player.networkHandler.getConnection().send(new ResourcePackStatusC2SPacket(packet.id(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
                        mc.player.networkHandler.getConnection().send(new ResourcePackStatusC2SPacket(packet.id(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
                    }))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to spoof accepting the recourse pack.")))
            );

            msg.append("\t").append(download).append(" ");
            msg.append(spoof);

            ChatUtils.sendHeliosMsg(msg);
            //mc.player.sendMessage(msg,false);
        }
    }

    static class RunnableEvent extends ClickEvent {
        final Runnable runnable;

        public RunnableEvent(Runnable runnable) {
            super(null, null);
            this.runnable = runnable;
        }
    }
}
