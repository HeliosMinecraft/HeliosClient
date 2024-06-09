package dev.heliosclient.mixin;

import dev.heliosclient.event.events.block.BlockShapeEvent;
import dev.heliosclient.managers.EventManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockCollisionSpliterator.class)
public class MixinBlockCollisionSpliterator {

    @Redirect(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"))
    private VoxelShape computeNext$getCollisionShape(BlockState blockState, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = blockState.getCollisionShape(world, pos, context);
        BlockShapeEvent event = new BlockShapeEvent(blockState, pos, shape);
        EventManager.postEvent(event);

        if (event.isCanceled()) {
            return VoxelShapes.empty();
        }

        return event.getShape();
    }
}