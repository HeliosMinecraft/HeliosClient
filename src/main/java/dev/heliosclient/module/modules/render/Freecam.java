package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.event.events.player.PlayerDeathEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.player.FreeCamEntity;
import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;


public class Freecam extends Module_ {
    private final SettingGroup sgGeneral = new SettingGroup("General");
    public Entity previousCamEntity = null;
    public FreeCamEntity freeCamEntity;
    DoubleSetting speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Speed")
            .description("Speed of the free cam")
            .onSettingChange(this)
            .value(1d)
            .defaultValue(1d)
            .min(0)
            .max(10)
            .roundingPlace(1)
            .build()
    );
    BooleanSetting rotateToInteract = sgGeneral.add(new BooleanSetting.Builder()
            .name("Rotate to interact")
            .description("Rotates the player to look at the pos when interacting or attacking blocks/entities")
            .onSettingChange(this)
            .value(true)
            .build()
    );
    BooleanSetting toggleOnEvent = sgGeneral.add(new BooleanSetting.Builder()
            .name("Toggle on event")
            .description("Automatically toggles freecam on certain events like disconnect or death")
            .onSettingChange(this)
            .value(false)
            .build()
    );


    public Freecam() {
        super("Freecam", "Allows you to move anywhere within your world", Categories.RENDER);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @SubscribeEvent
    public void onDeath(PlayerDeathEvent event) {
        if (toggleOnEvent.value)
            toggle();
    }

    @SubscribeEvent
    public void onDisconnect(PacketEvent.RECEIVE event) {
        if (toggleOnEvent.value && event.packet instanceof DisconnectS2CPacket)
            toggle();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.world == null) return;

        mc.chunkCullingEnabled = false;
        previousCamEntity = mc.getCameraEntity();
        freeCamEntity = new FreeCamEntity();

        freeCamEntity.spawn();

        mc.setCameraEntity(freeCamEntity);

        freeCamEntity.noClip = true;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.world == null) return;

        mc.chunkCullingEnabled = true;

        if (previousCamEntity != null) {
            mc.setCameraEntity(previousCamEntity);
        }
        freeCamEntity.remove();
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.SEND event) {
        if (event.getPacket() instanceof ClientCommandC2SPacket || event.getPacket() instanceof PlayerMoveC2SPacket) {
            event.setCanceled(true);
        }
    }

    public void onTick(TickEvent.PLAYER event) {
        if (rotateToInteract.value && mc.crosshairTarget != null) {
            RotationUtils.lookAt(mc.crosshairTarget);
        }
        FreeCamEntity.movementTick();
        FreeCamEntity.moveSpeed = (float) speed.value;
    }
}
