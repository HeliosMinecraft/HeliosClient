package dev.heliosclient.util;

import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ParticleUtils {
    public static ParticleType<?> stringToParticleType(String particleName) {
        Identifier id = new Identifier(particleName);
        return Registries.PARTICLE_TYPE.get(id);
    }
}
