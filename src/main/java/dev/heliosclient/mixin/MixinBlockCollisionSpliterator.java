package dev.heliosclient.mixin;

import com.google.common.collect.AbstractIterator;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.CollisionView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockCollisionSpliterator.class)
public abstract class MixinBlockCollisionSpliterator<T> extends AbstractIterator<T> {
    @Shadow @Final private CollisionView world;
    @Shadow @Final private BlockPos.Mutable pos;
    @Shadow @Final private ShapeContext context;
    @Unique
    private VoxelShape capturedVoxelShape;
    private BlockState capturedBlockShape;


    @Inject(method = "computeNext", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/BlockState;getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"))
    private void captureVoxelShape(CallbackInfoReturnable<T> cir, @Local BlockState blockState) {
        // Capture the voxelShape value
        this.capturedBlockShape = blockState;
        this.capturedVoxelShape = blockState.getCollisionShape(this.world, this.pos, this.context);
    }

    @Inject(method = "computeNext", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/shape/VoxelShape;offset(DDD)Lnet/minecraft/util/shape/VoxelShape;"))
    private void modifyVoxelShape(CallbackInfoReturnable<T> cir) {
        // Modify the voxelShape value here
        if (this.capturedVoxelShape != null) { // Replace with your condition
            this.capturedVoxelShape = VoxelShapes.empty(); // Replace with your desired VoxelShape
        }
    }
}