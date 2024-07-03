package dev.heliosclient.util.player;

import dev.heliosclient.HeliosClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;


public class FreeCamEntity extends OtherClientPlayerEntity {
    public static float moveSpeed = 1.0f;
    public static FreeCamEntity camEntity;
    private static float forwardRamped;
    private static float strafeRamped;
    private static float verticalRamped;
    private boolean ghost;

    public FreeCamEntity() {
        this(HeliosClient.MC.player);
    }

    public FreeCamEntity(PlayerEntity player) {
        this(player, player.getX(), player.getY(), player.getZ());
    }

    public FreeCamEntity(PlayerEntity player, double x, double y, double z) {
        super(HeliosClient.MC.world, player.getGameProfile());

        copyFrom(player);
        copyPositionAndRotation(player);
        this.setRotations(player.getYaw(), player.getPitch());

        // Cache the player textures, then switch to a random uuid
        // because the world doesn't allow duplicate uuids in 1.17+
        dataTracker.set(PLAYER_MODEL_PARTS, player.getDataTracker().get(PLAYER_MODEL_PARTS));
        setUuid(UUID.randomUUID());
        camEntity = this;
    }

    public static void movementTick() {
        FreeCamEntity camera = getCamEntity();

        if (camera != null) {
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientPlayerEntity player = mc.player;

            float forward = 0;
            float vertical = 0;
            float strafe = 0;

            GameOptions options = mc.options;
            if (options.forwardKey.isPressed()) {
                forward++;
            }
            if (options.backKey.isPressed()) {
                forward--;
            }
            if (options.leftKey.isPressed()) {
                strafe++;
            }
            if (options.rightKey.isPressed()) {
                strafe--;
            }
            if (options.jumpKey.isPressed()) {
                vertical++;
            }
            if (options.sneakKey.isPressed()) {
                vertical--;
            }

            float rampAmount = 0.15f;
            float speed = strafe * strafe + forward * forward;

            if (forward == 0 || strafe == 0) {
                speed = 1;
            } else {
                speed = (float) Math.sqrt(speed * 0.6);
            }

            forwardRamped = getRampedMotion(forwardRamped, forward, rampAmount) / speed;
            verticalRamped = getRampedMotion(verticalRamped, vertical, rampAmount);
            strafeRamped = getRampedMotion(strafeRamped, strafe, rampAmount) / speed;

            assert player != null;
            forward = player.isSprinting() ? forwardRamped * 2 : forwardRamped;

            camera.updateLastTickPosition();
            camera.handleMotion(forward, verticalRamped, strafeRamped);
        }
    }

    private static float getRampedMotion(float current, float input, float rampAmount) {
        if (input == 0) {
            current *= 0.5f;
        } else {
            if (input < 0) {
                rampAmount *= -1f;
            }

            if ((input < 0) != (current < 0)) {
                current = 0;
            }

            current = MathHelper.clamp(current + rampAmount, -1f, 1f);
        }

        return current;
    }

    private static double getMoveSpeed() {
        return moveSpeed;
    }

    public static FreeCamEntity getCamEntity() {
        return camEntity;
    }

    @Override
    public void attack(Entity target) {

    }

    private void handleMotion(float forward, float up, float strafe) {
        double xFactor = Math.sin(this.getYaw() * Math.PI / 180D);
        double zFactor = Math.cos(this.getYaw() * Math.PI / 180D);
        double scale = getMoveSpeed();

        double x = (strafe * zFactor - forward * xFactor) * scale;
        double y = (double) up * scale;
        double z = (forward * zFactor + strafe * xFactor) * scale;
        this.setVelocity(new Vec3d(x, y, z));

        this.move(MovementType.SELF, this.getVelocity());
    }

    private void updateLastTickPosition() {
        this.lastRenderX = this.getX();
        this.lastRenderY = this.getY();
        this.lastRenderZ = this.getZ();

        this.prevX = this.getX();
        this.prevY = this.getY();
        this.prevZ = this.getZ();

        this.prevYaw = this.getYaw();
        this.prevPitch = this.getPitch();

        this.prevHeadYaw = this.headYaw;
    }

    public void setRotations(float yaw, float pitch) {
        this.setYaw(yaw);
        this.setPitch(pitch);

        this.headYaw = this.getYaw();
    }

    public void spawn() {
        unsetRemoved();
        HeliosClient.MC.world.addEntity(this);
    }

    public void remove() {
        HeliosClient.MC.world.removeEntity(this.getId(), RemovalReason.DISCARDED);
    }

    public void setGhost(boolean ghost) {
        this.ghost = ghost;
    }

    @Override
    public boolean isSpectator() {
        return true;
    }

    @Override
    public boolean isInvisible() {
        return ghost || super.isInvisible();
    }

    @Override
    public boolean isInvisibleTo(PlayerEntity player) {
        return !ghost && super.isInvisibleTo(player);
    }
}