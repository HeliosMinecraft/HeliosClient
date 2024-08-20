package dev.heliosclient.module.modules.render.hiteffect.particles;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.world.ExplosionEvent;
import dev.heliosclient.event.listener.Listener;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.module.modules.render.hiteffect.HitEffectParticle;
import dev.heliosclient.util.BlockUtils;
import dev.heliosclient.util.render.Renderer3D;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.explosion.Explosion;

import java.awt.*;

public class OrbParticle extends HitEffectParticle implements Listener {
    public static boolean COOLER_PHYSICS = false;
    private final float radius;
    private final float gravity;
    MinecraftClient mc = MinecraftClient.getInstance();
    private Vec3d position;
    private Vec3d velocity;
    private float scale = 1.0f;
    private static final float FRICTION = 0.98f; // adjust friction strength as needed
    private static final float FLUID_BUOYANCY = 0.05f; // adjust fluid buoyancy strength as needed


    public OrbParticle(Vec3d position, Vec3d velocity, float radius, float gravity, float time_in_seconds, boolean hasRandomColor) {
        super((int) (time_in_seconds * 20), hasRandomColor);
        this.position = position;
        this.velocity = velocity;
        this.radius = radius;
        this.gravity = gravity;

        EventManager.register(this);
    }

    private void handleEntityCollision(Entity entity) {
        Box playerBox = entity.getBoundingBox().expand(0.2);
        if (playerBox.contains(position)) {
            Vec3d dir = position.subtract(entity.getPos()).normalize();

            velocity = velocity.add(dir.normalize().multiply(entity.getVelocity().length() / 5f).add(0,  COOLER_PHYSICS ? 0.025f : 0.002f, 0));
        }

        /*
        // Calculate the direction of the collision based on the relative positions of the orb and the entity
        Vec3d collisionPoint = this.position; // Approximate the collision point as the orb's position
        Vec3d collisionDirection = collisionPoint.subtract(entity.getPos()).normalize();

        // Apply a repulsion force in the direction of the collision
        this.velocity = this.velocity.add(collisionDirection.multiply(entity.getVelocity().length() / 5f ));

         */

    }

    @Override
    public void tick() {
        position = position.add(velocity);

        // Predict the next position
        Vec3d nextPos = position.add(velocity);

        BlockPos nextBlockPos = BlockUtils.toBlockPos(nextPos);

        BlockState blockState = mc.world.getBlockState(nextBlockPos);
        VoxelShape voxelShape = blockState.getCollisionShape(mc.world, nextBlockPos);

        // Check if the next position is inside a block
        if (!voxelShape.isEmpty()) {
            // Get the bounding box of the voxel shape
            Box voxelBox = voxelShape.getBoundingBox();

            if(AABBintersect(voxelBox)){
                velocity = new Vec3d(-velocity.x * 0.8, -velocity.y * 0.76, -velocity.z * 0.8);
            }

            if(velocity.y < 0.03f){
                velocity = new Vec3d(velocity.x,0,velocity.z);
            }
        } else {
            // If there's no collision, apply the velocity
            // Apply gravity
            velocity = velocity.subtract(0, gravity / 10f, 0);

            position = nextPos;
        }

        //A bit of friction
        velocity = velocity.multiply(FRICTION);

        // If the particle is in fluid, make it float upwards
        if (!blockState.getFluidState().isEmpty()) {
            velocity = velocity.add(0, FLUID_BUOYANCY, 0);
        }

        // Check for entity collision
        for (Entity entity : mc.world.getEntities()) {
            if (entity.getBoundingBox().expand(0.2).contains(position)) {
                // Handle entity collision
                handleEntityCollision(entity);
            }
        }

        /*
        // Check for collisions with the player and apply a repulsion force
        PlayerEntity player = HeliosClient.MC.player;
        Box playerBox = player.getBoundingBox();
        Box particleBox = new Box(position.x - radius - 0.1f, position.y - radius - 0.1f, position.z - radius - 0.1f, position.x + radius + 0.1f, position.y + radius + 0.1f, position.z + radius + 0.1f);
        if (playerBox.intersects(particleBox)) {
            Vec3d dir = player.getRotationVec(HeliosClient.MC.getTickDelta());

            velocity = velocity.add(dir.normalize().multiply(0.03).add(0, 0.025f, 0));
        }

         */


        current_age++;
        if (current_age >= life) {
            scale = Math.max(scale - mc.getLastFrameDuration() / 7f, 0);
            if (scale <= 0.0f) {
                discard();
            }
        }
    }

    public boolean AABBintersect(Box box) {
        // get box the closest point to sphere center by clamping
        double x = Math.max(box.minX, Math.min(position.x, box.maxX));
        double y = Math.max(box.minY, Math.min(position.y, box.maxY));
        double z = Math.max(box.minZ, Math.min(position.z, box.maxZ));

        // this is the same as isPointInsideSphere
        double distance = Math.sqrt(
                (x - position.x) * (x - position.x) +
                        (y - position.y) * (y - position.y) +
                        (z - position.z) * (z - position.z)
        );

        return distance < radius;
    }


    @Override
    public void discard() {
        super.discard();

        // Unregister the explosion listener
        EventManager.unregister(this);
    }

    @SubscribeEvent
    public void onExplosion(ExplosionEvent event) {
        Explosion explosion = event.getExplosion();

        Vec3d explosionPos = explosion.getPosition();

        // Calculate the direction from the explosion to the orb
        Vec3d dir = position.subtract(explosionPos);

        // Apply a force in the direction away from the explosion
        // The force decreases with the square of the distance to the explosion
        double distanceSq = dir.length();
        double force = (COOLER_PHYSICS ? 8.0 : 1.0) / (distanceSq + 0.1);  // Adjust the force as needed
        velocity = velocity.add(dir.normalize().multiply(force));
    }

    @Override
    public void render(MatrixStack matrixStack, Color color) {
        this.particleColor = hasRandomColor ? particleColor : color;

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


        matrixStack.scale(scale, scale, scale);

        Renderer3D.drawSphere(matrixStack, radius, 1.0f, particleColor);

        matrixStack.pop();
    }
}
