package dev.heliosclient.module.modules.render.hiteffect.particles;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.module.modules.render.hiteffect.HitEffectParticle;
import dev.heliosclient.util.render.Renderer3D;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;

public class OrbParticle extends HitEffectParticle {
    private final float radius;
    private final float gravity;
    MinecraftClient mc = MinecraftClient.getInstance();
    private Vec3d position;
    private Vec3d velocity;

    public OrbParticle(Vec3d position, Vec3d velocity, float radius, float gravity, float time_in_seconds) {
        super((int) (time_in_seconds * 20));
        this.position = position;
        this.velocity = velocity;
        this.radius = radius;
        this.gravity = gravity;
    }

    @Override
    public void tick() {
        super.tick();
        position = position.add(velocity);

        // Apply gravity
        velocity = velocity.add(0, -gravity / 10f, 0);

        // Predict the next position
        Vec3d nextPos = position.add(velocity);

        BlockPos nextBlockPos = new BlockPos((int) nextPos.x, (int) nextPos.y, (int) nextPos.z);

        BlockState blockState = HeliosClient.MC.world.getBlockState(nextBlockPos);
        VoxelShape voxelShape = blockState.getCollisionShape(HeliosClient.MC.world, nextBlockPos);

        // Check if the next position is inside a block
        if (!voxelShape.isEmpty()) {
            // Get the bounding box of the voxel shape
            Box voxelBox = voxelShape.getBoundingBox();

            // Check for horizontal and vertical collisions
            boolean horizontalCollision = voxelBox.intersects(nextPos.x, voxelBox.minY, nextPos.z, nextPos.x + velocity.x, voxelBox.maxY, nextPos.z + velocity.z);
            boolean verticalCollision = voxelBox.intersects(voxelBox.minX, nextPos.y, voxelBox.minZ, voxelBox.maxX, nextPos.y + velocity.y, voxelBox.maxZ);

            // If there is a horizontal collision, reverse the x and z velocities (bounce) and apply a damping factor
            if (horizontalCollision || !mc.world.getBlockState(new BlockPos((int) position.x, (int) position.y, (int) position.z)).isAir()) {
                velocity = new Vec3d(-velocity.x * 0.8, velocity.y, -velocity.z * 0.8);
            }

            // If there is a vertical collision, reverse the y velocity (bounce) and apply a damping factor
            if (verticalCollision || !blockState.isAir()) {
                velocity = new Vec3d(velocity.x, -velocity.y * 0.75, velocity.z);
            }
        } else {
            // If there's no collision, apply the velocity
            position = nextPos;
        }

        //A bit of friction
        velocity = velocity.multiply(0.90);

        // If the particle is in water, make it float upwards
        if (blockState.getFluidState().isIn(FluidTags.WATER)) {
            velocity = velocity.add(0, 0.02, 0);
        }

        // Check for collisions with the player and apply a repulsion force
        PlayerEntity player = HeliosClient.MC.player;
        Box playerBox = player.getBoundingBox();
        Box particleBox = new Box(position.x - radius - 0.1f, position.y - radius - 0.1f, position.z - radius - 0.1f, position.x + radius + 0.1f, position.y + radius + 0.1f, position.z + radius + 0.1f);
        if (playerBox.intersects(particleBox)) {
            Vec3d dir = player.getRotationVec(HeliosClient.MC.getTickDelta());

            velocity = velocity.add(dir.normalize().multiply(0.03).add(0, 0.025f, 0));
        }
    }

    @Override
    public void render(MatrixStack matrixStack, Color color) {
        matrixStack.push();
        final double posX = MathHelper.lerp(mc.getTickDelta(), position.x, position.x + velocity.x) - mc.getEntityRenderDispatcher().camera.getPos().getX();
        final double posY = MathHelper.lerp(mc.getTickDelta(), position.y, position.y + velocity.y) + 0.1 - mc.getEntityRenderDispatcher().camera.getPos().getY();
        final double posZ = MathHelper.lerp(mc.getTickDelta(), position.z, position.z + velocity.z) - mc.getEntityRenderDispatcher().camera.getPos().getZ();

        matrixStack.translate(posX, posY, posZ);

        matrixStack.scale(0.07f, 0.07f, 0.07f);

        matrixStack.translate(3f / 2, 3f / 2, 3f / 2);

        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));

        matrixStack.translate(-3f / 2, -3f / 2, -3f / 2);

        Renderer3D.drawSphere(matrixStack, radius, 1.0f, color);
        matrixStack.pop();
    }
}
