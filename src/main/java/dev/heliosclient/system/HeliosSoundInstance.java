package dev.heliosclient.system;

import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public class HeliosSoundInstance extends AbstractSoundInstance {
    public HeliosSoundInstance(SoundEvent sound, SoundCategory category, Random random) {
        super(sound, category, random);
    }

    public HeliosSoundInstance(Identifier soundId, SoundCategory category, Random random) {
        super(soundId, category, random);
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
