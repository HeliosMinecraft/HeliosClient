package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.DisconnectEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ChatUtils;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.network.NetworkUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import dev.heliosclient.util.timer.TickTimer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

public class FakeLag extends Module_ {
    private final Queue<Packet<?>> storedPackets = new LinkedList<>();
    private final SettingGroup sgGeneral = new SettingGroup("General");
    private Vec3d lagPos = Vec3d.ZERO;

    DoubleSetting timer = sgGeneral.add(new DoubleSetting.Builder()
            .name("Timer")
            .description("Automatically toggles FakeLag after the given delay (in ticks), 0 to never toggle")
            .onSettingChange(this)
            .value(0.0)
            .min(0.0)
            .max(1500d)
            .roundingPlace(0)
            .build()
    );
    BooleanSetting clearOnFlag = sgGeneral.add(new BooleanSetting.Builder()
            .name("Clear on flag")
            .description("Clears stored packets when server sends a player position packet")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    BooleanSetting burstMode = sgGeneral.add(new BooleanSetting.Builder()
            .name("Burst")
            .description("Sends packets after having reached a number of packets.")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    DoubleSetting burstMaxPackets = sgGeneral.add(new DoubleSetting.Builder()
            .name("Burst Max Packets")
            .description("When this limit is reached, it will automatically send all packets")
            .onSettingChange(this)
            .value(10)
            .min(0)
            .max(100)
            .roundingPlace(0)
            .shouldRender(()->burstMode.value)
            .build()
    );

    private final TickTimer toggleTimer = new TickTimer(true);

    public FakeLag() {
        super("FakeLag", "FakeLag aka Blink momentarily stops sending position packets and dumps them all at once to look like you were lagging", Categories.PLAYER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            lagPos = mc.player.getPos();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        sendPackets();
        lagPos = Vec3d.ZERO;
        toggleTimer.resetTimer();
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (timer.value > 0) {
            toggleTimer.increment();
        } else {
            return;
        }

        toggleTimer.every(timer.getInt(), this::toggle);
    }
    @SubscribeEvent
    public void onPacketSend(PacketEvent.SEND event) {
        if (mc.player == null || mc.player.isRiding()) {
            return;
        }
        if(burstMode.value && storedPackets.size() % (burstMaxPackets.getInt() * 5) == 0){
            sendPackets();
            lagPos = mc.player.getPos();
            return;
        }
        if (event.getPacket() instanceof PlayerActionC2SPacket
                || event.getPacket() instanceof PlayerMoveC2SPacket
                || event.getPacket() instanceof ClientCommandC2SPacket
                || event.getPacket() instanceof HandSwingC2SPacket
                || event.getPacket() instanceof PlayerInteractEntityC2SPacket
                || event.getPacket() instanceof PlayerInteractBlockC2SPacket
                || event.getPacket() instanceof PlayerInteractItemC2SPacket) {
            event.cancel();
            storedPackets.add(event.getPacket());
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.RECEIVE event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket && clearOnFlag.value) {
            storedPackets.clear();
        }
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent event) {
        Renderer3D.drawFlatFilledCircle(1f, lagPos, 320, QuadColor.gradient(ColorUtils.changeAlpha(Color.CYAN, 255).getRGB(), ColorUtils.changeAlpha(Color.WHITE, 255).getRGB(), QuadColor.CardinalDirection.SOUTH));
    }

    public void sendPackets() {
        if (mc.player != null && mc.player.networkHandler != null && !storedPackets.isEmpty()) {
            storedPackets.forEach(NetworkUtils::sendPacketNoEvent);
            storedPackets.clear();
        }
    }

    @Override
    public String getInfoString() {
        return storedPackets.isEmpty() ? "" : String.valueOf(storedPackets.size());
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);
        if((setting == burstMode || setting == timer) && burstMode.value && timer.value > 0.0){
            timer.setValue(0.0);
            ChatUtils.sendHeliosMsg(ColorUtils.red + "Burst Mode is on, Timer setting has been set to 0");
        }
    }
    @SubscribeEvent
    public void onDisconnect(DisconnectEvent e){
        this.onDisable();
    }

    public Vec3d getFakeLagPos(){
        return lagPos;
    }
}
