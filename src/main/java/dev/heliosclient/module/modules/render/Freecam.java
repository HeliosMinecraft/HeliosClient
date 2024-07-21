package dev.heliosclient.module.modules.render;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.event.events.player.PlayerDeathEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.Setting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.system.mixininterface.IPlayerInteractEntityC2SPacket;
import dev.heliosclient.util.player.FreeCamEntity;
import dev.heliosclient.util.player.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.util.math.Vec3d;


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
            .value(true)
            .build()
    );
    BooleanSetting blockOutOfRangePackets = sgGeneral.add(new BooleanSetting.Builder()
            .name("Block Out Of Range Packets")
            .description("Interaction packets with distance more than reach range may get you kicked, so this will cause them to not be sent.")
            .onSettingChange(this)
            .value(true)
            .build()
    );
    BooleanSetting blockAllHandSwing = sgGeneral.add(new BooleanSetting.Builder()
            .name("Block HandSwing")
            .description("All HandSwing packets will not be sent while in free cam.")
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
        if (mc.world == null || mc.player == null) {
            toggle();
            return;
        }

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
        }else if (event.getPacket() instanceof PlayerActionC2SPacket p) {
            PlayerActionC2SPacket.Action action = p.getAction();
            if(action != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK && action != PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) return;

            if (blockOutOfRangePackets.value && p.getPos().toCenterPos().distanceTo(mc.player.getEyePos()) >= mc.interactionManager.getReachDistance()) {
                event.setCanceled(true);
                return;
            }

            if (rotateToInteract.value)
                RotationUtils.lookAt(p.getPos().toCenterPos());

        } else if (event.getPacket() instanceof PlayerInteractBlockC2SPacket p) {
            Vec3d pos = p.getBlockHitResult().getPos();

            if (blockOutOfRangePackets.value && pos.distanceTo(mc.player.getEyePos()) >= mc.interactionManager.getReachDistance()) {
                event.setCanceled(true);
                return;
            }

            if (rotateToInteract.value)
                RotationUtils.lookAt(pos);
        } else if (event.getPacket() instanceof PlayerInteractEntityC2SPacket p) {
            Entity entity = ((IPlayerInteractEntityC2SPacket)p).getEntity();

            if (entity == null || entity == mc.player) {
                event.setCanceled(true);
                return;
            }

            if (blockOutOfRangePackets.value && entity.getPos().distanceTo(mc.player.getEyePos()) > mc.interactionManager.getReachDistance()) {
                event.setCanceled(true);
                return;
            }

            if (rotateToInteract.value)
                RotationUtils.lookAt(entity.getPos());
        }
        //Todo: Somehow block HandSwing packets sent after the interaction packets

        if (blockAllHandSwing.value && event.getPacket() instanceof HandSwingC2SPacket) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        FreeCamEntity.movementTick();
        FreeCamEntity.getCamEntity().updateInventory();
    }

    @Override
    public void onSettingChange(Setting<?> setting) {
        super.onSettingChange(setting);
        FreeCamEntity.moveSpeed = (float) speed.value;
    }
}
