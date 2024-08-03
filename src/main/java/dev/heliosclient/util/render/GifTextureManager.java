package dev.heliosclient.util.render;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.CapeManager;
import dev.heliosclient.util.FileUtils;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class GifTextureManager {
    private final LinkedList<Identifier> capeTextureIdentifiers = new LinkedList<>();
    private final LinkedList<Identifier> elytraTextureIdentifiers = new LinkedList<>();
    Set<File> gifFiles = new HashSet<>();

    public void registerGifTextures(File gifFile, String prefix) throws IOException {
        if(gifFiles.contains(gifFile))return;

        LinkedList<NativeImage> frames = FileUtils.readGifFrames(gifFile);
        HeliosClient.MC.execute(()->{
        for (int i = 0; i < frames.size(); i++) {
            NativeImageBackedTexture capeTexture = new NativeImageBackedTexture(CapeManager.parseCape(frames.get(i)));
            NativeImageBackedTexture elytraTexture = new NativeImageBackedTexture(frames.get(i));
            Identifier capeIdentifier = HeliosClient.MC.getTextureManager().registerDynamicTexture(prefix + "_cape_" + i, capeTexture);
            Identifier elytraIdentifier =  HeliosClient.MC.getTextureManager().registerDynamicTexture(prefix + "_elytra_" + i, elytraTexture);
            capeTextureIdentifiers.add(capeIdentifier);
            elytraTextureIdentifiers.add(elytraIdentifier);
        }
        gifFiles.add(gifFile);
        });
    }

    public LinkedList<Identifier> getCapeTextureIdentifiers() {
        return capeTextureIdentifiers;
    }

    public LinkedList<Identifier> getElytraTextureIdentifiers() {
        return elytraTextureIdentifiers;
    }
}
