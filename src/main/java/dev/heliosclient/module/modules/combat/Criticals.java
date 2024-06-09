package dev.heliosclient.module.modules.combat;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PlayerAttackEntityEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.DropDownSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.system.mixininterface.IVec3d;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.List;

public class Criticals extends Module_ {
    int tickTimer = 0;

    SettingGroup sgGeneral = new SettingGroup("General");

    DropDownSetting mode = sgGeneral.add(new DropDownSetting.Builder()
            .name("Mode")
            .onSettingChange(this)
            .value(List.of(Mode.values()))
            .defaultListOption(Mode.Packet)
            .build()
    );

    //Sometimes, it's better to have modes only react after certain amount of ticks on the next attack.
    //It helps with limiting flags.
    DoubleSetting timer = sgGeneral.add(new DoubleSetting.Builder()
            .name("Time")
            .description("Time for each mode to execute in ticks")
            .onSettingChange(this)
            .defaultValue(0d)
            .range(0, 120)
            .roundingPlace(0)
            .build()
    );

    public Criticals() {
        super("Criticals", "Makes every hit a critical attack", Categories.COMBAT);
        addSettingGroup(sgGeneral);
        addQuickSetting(mode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        tickTimer = 0;
    }

    @SubscribeEvent
    public void onAttack(PlayerAttackEntityEvent.PRE event) {
        if (skipCrit() || tickTimer < timer.value) return;

        tickTimer = 0;

        //Tanks again Meteor client
        switch ((Mode) mode.getOption()) {
            case Packet -> {
                sendPacket(0.2);
                sendPacket(0.01);
            }
            case Bypass -> {
                sendPacket(0.11);
                sendPacket(0.1100013579);
                sendPacket(0.0000013579);
            }
            case Falling -> {
                sendPacket(0.0625);
                sendPacket(0.0625013579);
                sendPacket(0.0000013579);
            }
            case Jump -> mc.player.jump();
            case MiniJump ->
                    ((IVec3d) mc.player.getVelocity()).heliosClient$set(mc.player.getVelocity().x, 0.30, mc.player.getVelocity().z);
            case Grim -> {
                // https://github.com/CCBlueX/LiquidBounce/blob/nextgen/src/main/kotlin/net/ccbluex/liquidbounce/features/module/modules/combat/ModuleCriticals.kt
                if (!mc.player.isOnGround()) {
                    // Requires packet type to be .FULL
                    sendPacket(-0.000001);
                }
            }

        }
    }

    @Override
    public void onTick(TickEvent event) {
        super.onTick(event);
        tickTimer++;
    }

    private boolean skipCrit() {
        return !mc.player.isOnGround() || mc.player.isSubmergedInWater() || mc.player.isInLava() || mc.player.isClimbing();
    }

    private void sendPacket(double height) {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.PositionAndOnGround(x, y + height, z, false);

        mc.player.networkHandler.sendPacket(packet);
    }

    public enum Mode {
        Packet,
        Bypass,
        Falling,
        MiniJump,
        Jump,
        Grim
    }
}
