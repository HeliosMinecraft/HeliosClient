package dev.heliosclient.util;

import net.minecraft.block.BedBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.function.Predicate;

public class EntityUtils {
    public static Entity getNearestEntity(World world, PlayerEntity player, double radius) {
        return world.getOtherEntities(player, new Box(player.getBlockPos()).expand(radius), entity -> true)
                .stream()
                .min((entity1, entity2) -> Float.compare(entity1.distanceTo(player), entity2.distanceTo(player)))
                .orElse(null);
    }

    public static Entity getNearestEntity(World world, PlayerEntity player, double radius, Predicate<Entity> filter, Comparator<Entity> sort) {
        return world.getOtherEntities(player, new Box(player.getBlockPos()).expand(radius), filter)
                .stream()
                .sorted(sort)
                .findFirst()
                .orElse(null);
    }

    public static Entity getNearestEntity(World world, PlayerEntity player, double radius, Predicate<Entity> filter) {
        return world.getOtherEntities(player, new Box(player.getBlockPos()).expand(radius), filter)
                .stream()
                .min((entity1, entity2) -> Float.compare(entity1.distanceTo(player), entity2.distanceTo(player)))
                .orElse(null);
    }

    public static Entity getNearestCrystal(World world, PlayerEntity player, double radius) {
        return world.getOtherEntities(player, new Box(player.getBlockPos()).expand(radius), entity -> entity.getType() == EntityType.END_CRYSTAL)
                .stream()
                .min((entity1, entity2) -> Float.compare(entity1.distanceTo(player), entity2.distanceTo(player)))
                .orElse(null);
    }

    public static BlockPos getNearestBed(World world, PlayerEntity player, int radius) {
        return BlockPos.streamOutwards(player.getBlockPos(), radius, radius, radius)
                .filter(pos -> world.getBlockState(pos).getBlock() instanceof BedBlock)
                .min((pos1, pos2) -> Float.compare(pos1.getManhattanDistance(player.getBlockPos()), pos2.getManhattanDistance(player.getBlockPos())))
                .orElse(null);
    }
}
