package dev.heliosclient.util.entity;

import dev.heliosclient.HeliosClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;


public class FreeCamEntity extends ClientPlayerEntity {
    public static float moveSpeed = 1.0f;
    public static FreeCamEntity camEntity;
    private boolean ghost;
    static MinecraftClient mc = MinecraftClient.getInstance();

    public FreeCamEntity() {
        this(HeliosClient.MC.player);
    }

    public FreeCamEntity(PlayerEntity player) {
        this(player, player.getX(), player.getY(), player.getZ());
    }


    public FreeCamEntity(PlayerEntity player, double x, double y, double z) {
        super(mc, mc.world, mc.player.networkHandler, mc.player.getStatHandler(), mc.player.getRecipeBook(),false,false);
        this.noClip = true;


        this.copyFrom(player);
        this.copyPositionAndRotation(player);
        this.setRotations(player.getYaw(), player.getPitch());
        this.setPos(x,y,z);
        this.input = mc.player.input;


        // Cache the player textures, then switch to a random uuid
        // because the world doesn't allow duplicate uuids in 1.17+
        dataTracker.set(PLAYER_MODEL_PARTS, player.getDataTracker().get(PLAYER_MODEL_PARTS));
        setUuid(UUID.randomUUID());
        this.refreshPosition();

        camEntity = this;
    }

    public void updateInventory() {
        PlayerInventory freeCamInventory = this.getInventory();

        for (int i = 0; i < 36; i++) {
            ItemStack playerStack = mc.player.getInventory().getStack(i);
            freeCamInventory.setStack(i, playerStack.copy());
        }
        freeCamInventory.selectedSlot = mc.player.getInventory().selectedSlot;
    }

    public static void movementTick() {
        FreeCamEntity camera = getCamEntity();
        ClientPlayerEntity player = mc.player;
        if (camera!= null && player!= null) {
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

            float speed = (float) getMoveSpeed();
            float xFactor = (float) Math.sin(camera.getYaw() * Math.PI / 180D);
            float zFactor = (float) Math.cos(camera.getYaw() * Math.PI / 180D);

            double x = (strafe * zFactor - forward * xFactor) * speed;
            double y = (double) vertical * speed;
            double z = (forward * zFactor + strafe * xFactor) * speed;

            // Update camera position smoothly
            camera.updateLastTickPosition();
            camera.setVelocity(new Vec3d(x,y,z));
            camera.move(MovementType.SELF, camera.getVelocity());
        }
    }

    @Override
    public boolean isUsingItem() {
        return HeliosClient.MC.player.isUsingItem();
    }

    public static double getMoveSpeed() {
        return moveSpeed;
    }

    public static FreeCamEntity getCamEntity() {
        return camEntity;
    }

    @Override
    public void attack(Entity target) {

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