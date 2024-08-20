package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.event.events.player.PostMovementUpdatePlayerEvent;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.modules.world.Timer;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.player.PlayerUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class TickShift extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    DoubleSetting timer = sgGeneral.add(new DoubleSetting.Builder()
            .name("Timer")
            .onSettingChange(this)
            .range(0, 2)
            .roundingPlace(2)
            .defaultValue(1)
            .build()
    );
    DoubleSetting packets = sgGeneral.add(new DoubleSetting.Builder()
            .name("Packets")
            .onSettingChange(this)
            .range(0, 120)
            .roundingPlace(0)
            .defaultValue(20)
            .build()
    );

    BooleanSetting shiftMode = sgGeneral.add(new BooleanSetting.Builder()
            .name("Shift Mode Timer")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    DoubleSetting ticksToShift = sgGeneral.add(new DoubleSetting.Builder()
            .name("Ticks To Shift")
            .onSettingChange(this)
            .range(0, 1200)
            .roundingPlace(0)
            .defaultValue(20)
            .shouldRender(()->shiftMode.value)
            .build()
    );

    BooleanSetting cancelGround = sgGeneral.add(new BooleanSetting.Builder()
            .name("Cancel Ground")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    private int ticks;

    public TickShift() {
        super("TickShift", "Magically shifts you a number of ticks while you are standing", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ModuleManager.get(Timer.class).setOverride(Timer.RESET);
        ticks = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ModuleManager.get(Timer.class).setOverride(Timer.RESET);
        ticks = 0;
    }
    @SubscribeEvent
    public void packetSend(PacketEvent.SEND e) {
        if(e.isCanceled())return;

        if (e.getPacket() instanceof PlayerMoveC2SPacket.Full) {
            shift(e.isCanceled(),true);
        }
        if (e.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
            shift(e.isCanceled(),true);
        }

        if (e.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround pac) {
            if (cancelGround.value || pac.isOnGround() == mc.player.isOnGround())
                e.setCanceled(true);
            else{
                shift(e.isCanceled(),false);
            }
        }

        if (e.getPacket() instanceof PlayerMoveC2SPacket.OnGroundOnly p) {
            if (cancelGround.value)
                e.setCanceled(true);
            else{
                shift(e.isCanceled(),false);
            }
        }

        ticks = ticks <= 0 ? 0 : ticks - 1;
    }

    @SubscribeEvent
    public void onPlayerMovementUpdatePost(PostMovementUpdatePlayerEvent event) {
        if(!shiftMode.value)return;

        if(PlayerUtils.isMoving(mc.player)){
            ChatUtils.sendHeliosMsg("You are supposed to stand still.. Disabling");
            toggle();
            return;
        }

        mc.options.forwardKey.setPressed(true);
        event.setCanceled(true);
        event.setNumberOfTicks((int) ticksToShift.value);

        ChatUtils.sendHeliosMsg("TickShifted.. Disabling");
        mc.options.forwardKey.setPressed(false);
        toggle();
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if(!PlayerUtils.isMoving(mc.player)){
            ModuleManager.get(Timer.class).setOverride(Timer.RESET);
            ticks = ticks >= packets.value ? (int) packets.value : ticks + 1;
        }
    }
    public void shift(boolean canceled,boolean moving){
        if(canceled)return;

        if (moving && PlayerUtils.isMoving(mc.player) && ticks > 0)
            ModuleManager.get(Timer.class).setOverride(timer.value);

        ticks = ticks <= 0 ? 0 : ticks - 1;
    }

    @Override
    public String getInfoString() {
        return String.valueOf(ticks);
    }
}
