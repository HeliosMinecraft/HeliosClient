package dev.heliosclient.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(MapState.class)
public interface AccessorMapState {
    @Accessor("updateTrackers")
    List<MapState.PlayerUpdateTracker> getUpdateTrackers();

    @Accessor("updateTrackersByPlayer")
    Map<PlayerEntity, MapState.PlayerUpdateTracker> getUpdateTrackersByPlayer();
}
