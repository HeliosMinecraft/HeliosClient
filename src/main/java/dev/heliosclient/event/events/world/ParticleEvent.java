package dev.heliosclient.event.events.world;

import dev.heliosclient.event.Cancelable;
import dev.heliosclient.event.Event;
import net.minecraft.particle.ParticleEffect;

@Cancelable
public class ParticleEvent extends Event {

    public final ParticleEffect parameters;
    public final double x, y, z;

    public ParticleEvent(ParticleEffect parameters, double x, double y, double z) {
        this.parameters = parameters;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public ParticleEffect getParameters() {
        return parameters;
    }
}
