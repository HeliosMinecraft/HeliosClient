package dev.heliosclient.util;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.system.HeliosSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundUtils {
    public static final Identifier TING_SOUND = Identifier.of("heliosclient:ting");
    public static final Identifier CLICK_SOUND = Identifier.of("heliosclient:click");
    public static SoundEvent TING_SOUNDEVENT = SoundEvent.of(TING_SOUND);
    public static SoundEvent CLICK_SOUNDEVENT = SoundEvent.of(CLICK_SOUND);

    public static void playSound(SoundEvent sound, float volume, float pitch) {
        if (HeliosClient.MC.world == null || HeliosClient.MC.player == null) return;
        HeliosClient.MC.world.playSound(HeliosClient.MC.player, HeliosClient.MC.player.getBlockPos(), sound, SoundCategory.PLAYERS, volume, pitch);
    }

    public static void playInstanceSound(SoundEvent sound) {
        SoundInstance soundInstance = new HeliosSoundInstance(sound, SoundCategory.PLAYERS, SoundInstance.createRandom());
        HeliosClient.MC.getSoundManager().play(soundInstance);
    }

    public static void playInstanceSound(SoundEvent sound, float volume, float pitch) {
        HeliosSoundInstance soundInstance = new HeliosSoundInstance(sound, SoundCategory.PLAYERS, SoundInstance.createRandom());
        soundInstance.setPitch(pitch);
        soundInstance.setVolume(volume);
        HeliosClient.MC.getSoundManager().play(soundInstance);
    }

    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, TING_SOUND, TING_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, CLICK_SOUND, CLICK_SOUNDEVENT);
    }
}
