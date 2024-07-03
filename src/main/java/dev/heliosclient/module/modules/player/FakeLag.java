package dev.heliosclient.module.modules.player;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.event.events.render.Render3DEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.render.Renderer3D;
import dev.heliosclient.util.render.color.QuadColor;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FakeLag extends Module_ {
    private final List<PlayerMoveC2SPacket> storedPackets = new CopyOnWriteArrayList<>();
    private final SettingGroup sgGeneral = new SettingGroup("General");
    Vec3d lagPos = Vec3d.ZERO;
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
    private int toggleTimer = -1;

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
        toggleTimer = 0;
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.HIGHEST)
    public void onTick(TickEvent.PLAYER event) {
        if (timer.value > 0) {
            toggleTimer++;
        } else {
            return;
        }

        if (toggleTimer > timer.value) {
            toggle();
            toggleTimer = 0;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.SEND event) {
        if (!(event.packet instanceof PlayerMoveC2SPacket packet)) return;
        event.setCanceled(true);
        PlayerMoveC2SPacket previous = storedPackets.isEmpty() ? null : storedPackets.get(storedPackets.size() - 1);

        //Prevents adding multiple same packets.
        if (previous != null &&
                previous.getX(-1) == packet.getX(-1) &&
                previous.getY(-1) == packet.getY(-1) &&
                previous.getZ(-1) == packet.getZ(-1) &&
                previous.getYaw(-1) == packet.getYaw(-1) &&
                previous.getPitch(-1) == packet.getPitch(-1) &&
                previous.isOnGround() == packet.isOnGround()
        ) return;

        storedPackets.add(packet);
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
        if (mc.player != null && mc.getNetworkHandler() != null) {
            storedPackets.forEach(mc.player.networkHandler::sendPacket);
            storedPackets.clear();
        }
    }
}
