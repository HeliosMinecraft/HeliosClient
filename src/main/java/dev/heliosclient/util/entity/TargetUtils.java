package dev.heliosclient.util.entity;

import dev.heliosclient.util.misc.SortMethod;
import dev.heliosclient.util.player.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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

    public TargetUtils setRange(double range) {
        this.range = range;
        return this;
    }

    public TargetUtils setFilter(Predicate<Entity> filter) {
        this.filter = filter;
        return this;
    }

    public Entity getTarget(PlayerEntity player) {
        return currentTarget = EntityUtils.getNearestEntity(
                player.getWorld(),
                player,
                range,
                filter.and(entity -> !(entity instanceof FreeCamEntity)),
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
        getTarget(mc.player);

        if (currentTarget != null) {
            run.accept(currentTarget);
        }

        return currentTarget;
    }

    public boolean found(){
        return currentTarget != null && this.filter.test(currentTarget);
    }

    public boolean isLivingEntity(){
        return currentTarget instanceof LivingEntity;
    }

    public Entity findTarget(Consumer<Entity> run, Predicate<Entity> filter) {
        setFilter(filter);

        return findTarget(run);
    }
    public TargetUtils sortMethod(SortMethod sortMethod) {
        this.sortMethod = sortMethod;
        return this;
    }
}