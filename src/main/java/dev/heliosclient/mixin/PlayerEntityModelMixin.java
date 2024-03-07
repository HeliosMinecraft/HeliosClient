package dev.heliosclient.mixin;

import com.google.common.base.Enums;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.*;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin {

    @Unique
    Set<Direction> set = EnumSet.allOf(Direction.class);
    @Inject(method = "setAngles*", at = @At("HEAD"))
    private void modifyCloak(PlayerEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo info) {
        ModelPart cloak = ((AccessorPlayerEntityModel)this).getCloak();

        try {
            Field cuboidsField = ModelPart.class.getDeclaredField("cuboids");
            cuboidsField.setAccessible(true);

            List<ModelPart.Cuboid> newCuboids = new ArrayList<>();
            // Add multiple smaller cuboids to approximate a rounded shape
            for (int i = 0; i < 10; i++) {
                float x = -5.0F + i;
                newCuboids.add(new ModelPart.Cuboid(0, 0, x, 0.0F, -1.0F, 1, 16, 1, 0, 0, 0, false, 64, 64,set));
            }

            cuboidsField.set(cloak, newCuboids);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }
}
