package dev.heliosclient.module.modules.movement;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.TickEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.CycleSetting;
import dev.heliosclient.module.settings.DoubleSetting;
import dev.heliosclient.module.settings.SettingGroup;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class BoatFly extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    CycleSetting mode = sgGeneral.add(new CycleSetting.Builder()
            .name("Mode")
            .description("Mode of boat fly")
            .onSettingChange(this)
            .value(List.of(Mode.values()))
            .defaultListOption(Mode.Velocity)
            .build()
    );
    DoubleSetting horizontalSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Horizontal Speed")
            .description("Horizontal speed of the boat")
            .onSettingChange(this)
            .value(0.7d)
            .range(0, 75)
            .roundingPlace(1)
            .build()
    );
    DoubleSetting verticalSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Vertical Speed")
            .description("Vertical speed of the boat")
            .onSettingChange(this)
            .value(0.5d)
            .range(0, 75)
            .roundingPlace(1)
            .build()
    );
    DoubleSetting fallSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("Fall Speed")
            .description("Falling speed of the boat")
            .onSettingChange(this)
            .value(0.08d)
            .range(0, 10)
            .roundingPlace(2)
            .build()
    );
    BooleanSetting onGround = sgGeneral.add(new BooleanSetting.Builder()
            .name("Boat onGround")
            .description("Sets boat onGround flag as true")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting cancelServerPackets = sgGeneral.add(new BooleanSetting.Builder()
            .name("Cancel Server packets")
            .description("Cancels incoming boat movement packets from the server")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting transformYaw = sgGeneral.add(new BooleanSetting.Builder()
            .name("Transform Yaw")
            .description("Sets the boat yaw to player yaw")
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting stopInUnloaded = sgGeneral.add(new BooleanSetting.Builder()
            .name("Stop in unloaded")
            .description("Stops the boat from moving in unloaded chunks")
            .defaultValue(true)
            .onSettingChange(this)
            .build()
    );
    BooleanSetting phaseSimple = sgGeneral.add(new BooleanSetting.Builder()
            .name("Phase")
            .description("A very simple boat phase")
            .defaultValue(false)
            .onSettingChange(this)
            .build()
    );

    public BoatFly() {
        super("BoatFly", "Lets you fly your boat", Categories.MOVEMENT);
        addSettingGroup(sgGeneral);
        addQuickSettings(sgGeneral.getSettings());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (phaseSimple.value) {
            if (mc.player.getControllingVehicle() != null) {
                mc.player.getControllingVehicle().noClip = false;
                mc.player.getControllingVehicle().setNoGravity(false);
            }
            mc.player.noClip = false;
            mc.player.setNoGravity(false);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PLAYER event) {
        if (!mc.player.hasVehicle() || mc.player.getControllingVehicle() == null) return;

        if (!(mc.player.getControllingVehicle() instanceof BoatEntity boatEntity)) return;

        if (transformYaw.value) {
            boatEntity.setYaw(mc.player.getYaw());
        }
        //Stop in unloaded chunks
        if ((!mc.world.isChunkLoaded((int) boatEntity.getPos().getX() >> 4, (int) boatEntity.getPos().getZ() >> 4) || boatEntity.getPos().getY() < -60) && stopInUnloaded.value) {
            boatEntity.setVelocity(0, 0, 0);
            return;
        }
        if (phaseSimple.value) {
            boatEntity.noClip = true;
            boatEntity.setNoGravity(false);
            mc.player.noClip = true;
            mc.player.setNoGravity(false);
        }

        if (mode.getOption() == Mode.Velocity) {
            Vec3d velocity = boatEntity.getVelocity();
            double directionX = velocity.x;
            double directionY = 0;
            double directionZ = velocity.z;

            if (mc.options.jumpKey.isPressed()) {
                directionY = verticalSpeed.value;
            } else if (mc.options.sprintKey.isPressed()) {
                directionY = -verticalSpeed.value;
            }
            directionY -= fallSpeed.value;

            if (mc.options.forwardKey.isPressed()) {
                float yawRad = boatEntity.getYaw() * MathHelper.RADIANS_PER_DEGREE;
                directionX = MathHelper.sin(-yawRad) * horizontalSpeed.value;
                directionZ = MathHelper.cos(yawRad) * horizontalSpeed.value;
            }

            boatEntity.setVelocity(directionX, directionY, directionZ);
        } else {
            Vec3d position = boatEntity.getPos();
            double nextPosX = position.x;
            double nextPosY = position.y;
            double nextPosZ = position.z;

            if (mc.options.jumpKey.isPressed()) {
                nextPosY += verticalSpeed.value;
            } else if (mc.options.sprintKey.isPressed()) {
                nextPosY -= verticalSpeed.value;
            }
            nextPosY -= fallSpeed.value;

            if (mc.options.forwardKey.isPressed()) {
                float yawRad = boatEntity.getYaw() * MathHelper.RADIANS_PER_DEGREE;
                nextPosX += MathHelper.sin(-yawRad) * horizontalSpeed.value;
                nextPosZ += MathHelper.cos(yawRad) * horizontalSpeed.value;
            }
            boatEntity.setPosition(nextPosX, nextPosY, nextPosZ);
            boatEntity.setOnGround(onGround.value);
            mc.player.networkHandler.sendPacket(new VehicleMoveC2SPacket(boatEntity.getPos(),boatEntity.getYaw(),boatEntity.getPitch(),onGround.value));
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.RECEIVE event) {
        if ((event.packet instanceof VehicleMoveS2CPacket ||
                event.packet instanceof PlayerMoveC2SPacket ||
                event.packet instanceof EntityS2CPacket
        ) && cancelServerPackets.value) {
            event.setCanceled(true);
        }
    }

    public enum Mode {
        Velocity,
        Packet
    }
}
