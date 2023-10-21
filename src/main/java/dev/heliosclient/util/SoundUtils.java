package dev.heliosclient.util;

import dev.heliosclient.HeliosClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundUtils {
    public static final Identifier TING_SOUND = new Identifier("heliosclient:ting");
    public static SoundEvent TING_SOUNDEVENT = SoundEvent.of(TING_SOUND);

    public static void playSound(SoundEvent sound, float volume, float pitch) {
        if (HeliosClient.MC.world == null || HeliosClient.MC.player == null) return;
        HeliosClient.MC.world.playSound(HeliosClient.MC.player, HeliosClient.MC.player.getBlockPos(), sound, SoundCategory.BLOCKS, volume, pitch);
    }

    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, TING_SOUND, TING_SOUNDEVENT);
    }
}
