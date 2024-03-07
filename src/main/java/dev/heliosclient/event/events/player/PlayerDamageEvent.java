package dev.heliosclient.event.events.player;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.LuaEvent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

@Cancelable
@LuaEvent("PlayerDamageEvent")
public class PlayerDamageEvent extends Event {

    private final PlayerEntity player;

    private final DamageSource damageSource;

    public PlayerDamageEvent(PlayerEntity player, DamageSource damageSource) {
        this.player = player;
        this.damageSource = damageSource;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }
}
