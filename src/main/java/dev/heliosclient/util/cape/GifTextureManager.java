package dev.heliosclient.util.cape;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.CapeManager;
import dev.heliosclient.util.FileUtils;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class GifTextureManager {
    private final LinkedList<Identifier> capeTextureIdentifiers = new LinkedList<>();
    private final LinkedList<Identifier> elytraTextureIdentifiers = new LinkedList<>();

    public void registerGifTextures(File gifFile, String prefix) throws IOException {
        LinkedList<NativeImage> frames = FileUtils.readGifFrames(gifFile);
        HeliosClient.MC.execute(()->{
        for (int i = 0; i < frames.size(); i++) {
            NativeImageBackedTexture capeTexture = new NativeImageBackedTexture(CapeManager.parseCape(frames.get(i)));
            NativeImageBackedTexture elytraTexture = new NativeImageBackedTexture(frames.get(i));

            Identifier capeIdentifier = Identifier.of(prefix + "_cape_" + i);
            Identifier elytraIdentifier = Identifier.of(prefix + "_elytra_" + i);

            HeliosClient.MC.getTextureManager().registerTexture(capeIdentifier, capeTexture);
            HeliosClient.MC.getTextureManager().registerTexture(elytraIdentifier, elytraTexture);

            capeTextureIdentifiers.add(capeIdentifier);
            elytraTextureIdentifiers.add(elytraIdentifier);
        }
        });
    }
    public void discardAll(){
        capeTextureIdentifiers.clear();
        elytraTextureIdentifiers.clear();
    }

    public LinkedList<Identifier> getCapeTextureIdentifiers() {
        return capeTextureIdentifiers;
    }

    public LinkedList<Identifier> getElytraTextureIdentifiers() {
        return elytraTextureIdentifiers;
    }
}
