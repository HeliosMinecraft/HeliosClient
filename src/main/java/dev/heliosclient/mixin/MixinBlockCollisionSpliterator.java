package dev.heliosclient.mixin;

import com.google.common.collect.AbstractIterator;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.events.block.BlockShapeEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.CollisionView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockCollisionSpliterator.class)
public abstract class MixinBlockCollisionSpliterator<T> extends AbstractIterator<T> {
    @WrapOperation(method = "computeNext",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/ShapeContext;getCollisionShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/CollisionView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;"
            )
    )
    private VoxelShape onComputeNext(ShapeContext instance, BlockState state, CollisionView collisionView, BlockPos blockPos, Operation<VoxelShape> original) {
        VoxelShape shape = original.call(instance, state, collisionView, blockPos);

        if (collisionView != HeliosClient.MC.world) {
            return shape;
        }

        BlockShapeEvent event = (BlockShapeEvent) EventManager.postEvent(new BlockShapeEvent(state, blockPos, shape));
        return event.isCanceled() ? event.getShape() : shape;
    }
}