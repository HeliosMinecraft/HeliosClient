package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.event.events.player.SendMovementPacketEvent;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.world.Timer;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.player.MovementUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class TickShift extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    DoubleSetting ticksToShift = sgGeneral.add(new DoubleSetting.Builder()
            .name("Ticks To Shift")
            .onSettingChange(this)
            .range(1, 60)
            .roundingPlace(0)
            .defaultValue(20)
            .value(20)
            .build()
    );
    DoubleSetting packets = sgGeneral.add(new DoubleSetting.Builder()
            .name("Packets")
            .description("Packets to release every tick")
            .onSettingChange(this)
            .range(1, 10)
            .roundingPlace(0)
            .defaultValue(1)
            .value(1)
            .build()
    );
    DoubleSetting tickChargeSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Ticks Charge Speed")
            .description("Amount of ticks to charge every player tick")
            .onSettingChange(this)
            .range(1, 10)
            .roundingPlace(0)
            .defaultValue(1)
            .value(1)
            .build()
    );
    BooleanSetting cancelGround = sgGeneral.add(new BooleanSetting.Builder()
            .name("Cancel Ground")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    private int packetCount;
    public TickShift() {
        super("TickShift", "Magically shifts you a number of ticks while you are standing", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ModuleManager.get(Timer.class).setOverride(Timer.RESET);
        packetCount = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ModuleManager.get(Timer.class).setOverride(Timer.RESET);
        packetCount = 0;
    }

    @SubscribeEvent
    public void packetSend(PacketEvent.SEND e) {
        if(e.isCanceled()) return;

        if (e.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround pac) {
            if (cancelGround.value || pac.isOnGround() == mc.player.isOnGround())
                e.setCanceled(true);
        }

        if (e.getPacket() instanceof PlayerMoveC2SPacket.OnGroundOnly p) {
            if (cancelGround.value)
                e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPreSendMovementUpdate(SendMovementPacketEvent.PRE event) {
        if(MovementUtils.isMoving(mc.player) || !mc.player.isOnGround()){
            packetCount -= packets.getInt();
            if (packetCount <= 0) {
                packetCount = 0;
                ModuleManager.get(Timer.class).setOverride(Timer.RESET);
                return;
            }

            ModuleManager.get(Timer.class).setOverride(packets.value + 1);
        } else{
            packetCount = packetCount >= ticksToShift.getInt() ? ticksToShift.getInt() : packetCount + tickChargeSpeed.getInt();
        }
    }

    @Override
    public String getInfoString() {
        return String.valueOf(packetCount);
    }
}
