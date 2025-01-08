package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.player.PlayerUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

import java.util.List;

public class AntiVoid extends Module_ {
    public SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting fallCheck = sgGeneral.add(new BooleanSetting.Builder()
            .name("Fall Check")
            .description("Tries to reset your position if you are falling more than 5 blocks")
            .onSettingChange(this)
            .value(false)
            .build()
    );

    BooleanSetting packetCancel = sgGeneral.add(new BooleanSetting.Builder()
            .name("Packet Cancel")
            .description("Cancels ongoing/incoming packets with `y` less than world's bottom Y (aka void y level)")
            .onSettingChange(this)
            .value(true)
            .build()
    );
    CycleSetting cancelMode = sgGeneral.add(new CycleSetting.Builder()
            .name("Cancel Mode")
            .description("Cancel mode for position packets")
            .onSettingChange(this)
            .defaultValue(List.of(CancelMode.values()))
            .defaultListOption(CancelMode.OnGoing)
            .addOptionToolTip("Packets coming from the server")
            .addOptionToolTip("Packets going to the server")
            .addOptionToolTip("Cancel both incoming and ongoing packets to the sever.")
            .shouldRender(()->packetCancel.value)
            .build()
    );

    BooleanSetting position = sgGeneral.add(new BooleanSetting.Builder()
            .name("Position Change")
            .description("Sets your current positions to previous position")
            .onSettingChange(this)
            .value(false)
            .build()
    );

    BooleanSetting packetSpoof = sgGeneral.add(new BooleanSetting.Builder()
            .name("Packet Spoof")
            .description("Sends Packets of your previous positions in attempt to rubberband")
            .onSettingChange(this)
            .value(false)
            .build()
    );
    BooleanSetting onGround = sgGeneral.add(new BooleanSetting.Builder()
            .name("OnGround Spoof")
            .description("OnGround boolean for Position packet sent")
            .onSettingChange(this)
            .value(false)
            .shouldRender(()->packetSpoof.value)
            .build()
    );

    public AntiVoid() {
        super("AntiVoid", "Attempt to save you from falling into the void.", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if(mc.player.getY() < mc.world.getBottomY() || fallCheck()) {
            if (position.value) {
                mc.player.setPosition(mc.player.prevX, mc.player.prevY, mc.player.prevZ);
            }

            if (packetSpoof.value && mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().getConnection().send(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.prevX,mc.player.prevY,mc.player.prevZ,onGround.value,mc.player.horizontalCollision));
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.SEND event){
        if(packetCancel.value && (cancelMode.isOption(CancelMode.Both) || cancelMode.isOption(CancelMode.OnGoing)) && event.getPacket() instanceof PlayerMoveC2SPacket pp && mc.world != null){
            if(pp.changesPosition() && pp.getY(mc.player.getY()) < mc.world.getBottomY() || fallCheck()){
                event.cancel();
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.RECEIVE event){
        if(packetCancel.value && (cancelMode.isOption(CancelMode.Both) || cancelMode.isOption(CancelMode.Incoming)) && event.getPacket() instanceof PlayerPositionLookS2CPacket pp && mc.world != null){
            if(pp.change().position().y < mc.world.getBottomY() || fallCheck()){
                event.cancel();
            }
        }
    }

    private boolean fallCheck(){
        if(!fallCheck.value){
            return false;
        }

        return PlayerUtils.willFallMoreThanFiveBlocks(mc.player.getPos());
    }

    public enum CancelMode{
        Incoming,
        OnGoing,
        Both
    }
}
