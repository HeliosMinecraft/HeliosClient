package dev.heliosclient.util.entity;

import dev.heliosclient.util.SortMethod;
import dev.heliosclient.util.player.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static dev.heliosclient.util.player.PlayerUtils.mc;

public class TargetUtils {
    //Common shared across modules
    private static TargetUtils instance;
    public SortMethod sortMethod = SortMethod.LowestDistance;
    @Nullable
    public Entity currentTarget = null;
    private double range;
    private Predicate<Entity> filter = (entity) -> true;


    public TargetUtils() {
    }

    public static TargetUtils getInstance() {
        if (instance == null) {
            instance = new TargetUtils();
        }
        return instance;
    }

    public static boolean isEntityVisible(Entity entity) {
        return PlayerUtils.canSeeEntity(entity);
    }

    public void setRange(double range) {
        this.range = range;
    }

    public void setFilter(Predicate<Entity> filter) {
        this.filter = filter;
    }

    public Entity getTarget(PlayerEntity player) {
        return currentTarget = EntityUtils.getNearestEntity(
                player.getWorld(),
                player,
                range,
                filter,
                sortMethod);
    }

    public Entity getTarget(PlayerEntity player, Predicate<Entity> filter) {
        setFilter(filter);

        return getTarget(player);
    }

    public Entity getNewTargetIfNull(Predicate<Entity> filter, boolean applyFilter) {
        setFilter(filter);

        //If current target is not found or the filter result for currentTarget is not satisfying then get a new one.
        if (currentTarget == null || !currentTarget.isAlive() || (applyFilter && !this.filter.test(currentTarget))) {
            return getTarget(mc.player);
        }

        return currentTarget;
    }

    public Entity getNewTargetIfNull(boolean applyFilter) {
        if (currentTarget == null || (applyFilter && !this.filter.test(currentTarget))) {
            return getTarget(mc.player);
        }

        return currentTarget;
    }

    public Entity findTarget(Consumer<Entity> run) {
        currentTarget = EntityUtils.getNearestEntity(
                mc.world,
                mc.player,
                range,
                filter,
                sortMethod);

        if (currentTarget != null) {
            run.accept(currentTarget);
        }

        return currentTarget;
    }

    public Entity findTarget(Consumer<Entity> run, Predicate<Entity> filter) {

        setFilter(filter);

        return findTarget(run);
    }

    public void setSortMethod(SortMethod sortMethod) {
        this.sortMethod = sortMethod;
    }
}