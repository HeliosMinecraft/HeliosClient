package dev.heliosclient.util.cape;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.CapeManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

//Space complexity  = o(infinite)
public class CapeTextureManager {
    public final Map<String, LinkedList<Identifier>> gifCapeTextures = new HashMap<>();
    private final Map<String, LinkedList<Identifier>> gifElytraTextures = new HashMap<>();

    public final Map<String, Identifier> staticCapeTextures = new HashMap<>();
    private final Map<String, Identifier> staticElytraTextures = new HashMap<>();

    public final Map<UUID, String> playerCapes = new HashMap<>();
    private final Map<UUID, Integer> currentFrame = new HashMap<>();

    private Timer timer = new Timer("CapeTextureManager");

    public CapeTextureManager() {
        gifCapeTextures.clear();
        gifElytraTextures.clear();
        this.startCapeAnimation();
    }

    public void registerCapeTextures(InputStream stream, File capeFile, String prefix) throws IOException {
        if (capeFile.getName().toLowerCase().endsWith(".gif")) {
            final GifTextureManager gifTextureManager = new GifTextureManager();
            gifTextureManager.registerGifTextures(capeFile, prefix);
            gifCapeTextures.put(prefix, gifTextureManager.getCapeTextureIdentifiers());
            gifElytraTextures.put(prefix, gifTextureManager.getElytraTextureIdentifiers());
            gifTextureManager.discardAll();
        } else {
            NativeImage image = NativeImage.read(stream);
            HeliosClient.MC.execute(()->{
                Identifier capeIdentifier =  HeliosClient.MC.getTextureManager().registerDynamicTexture(prefix + "_cape", new NativeImageBackedTexture(CapeManager.parseCape(image)));
                Identifier elytraIdentifier =  HeliosClient.MC.getTextureManager().registerDynamicTexture(prefix + "_elytra", new NativeImageBackedTexture(image));
                staticCapeTextures.put(prefix, capeIdentifier);
                staticElytraTextures.put(prefix, elytraIdentifier);
            });
        }
    }


    public void assignCapeToPlayer(UUID playerUUID, String capeName) {
        playerCapes.put(playerUUID, capeName);
        currentFrame.put(playerUUID, 0);
    }

    public Identifier getCurrentCapeTexture(UUID playerUUID) {
        String capeName = playerCapes.get(playerUUID);
        if (capeName == null) {
            return CapeManager.DEFAULT_CAPE_TEXTURE;
        }

        if (gifCapeTextures.containsKey(capeName)) {
            List<Identifier> frames = gifCapeTextures.get(capeName);
            int frameIndex = currentFrame.get(playerUUID);
            return frames.get(frameIndex);
        } else {
            return staticCapeTextures.getOrDefault(capeName, CapeManager.DEFAULT_CAPE_TEXTURE);
        }
    }
    public Identifier getStaticCapeTexture(String capeName) {
        return staticCapeTextures.getOrDefault(capeName, CapeManager.DEFAULT_CAPE_TEXTURE);
    }

    public Identifier getCurrentElytraTexture(UUID playerUUID) {
        String capeName = playerCapes.get(playerUUID);
        if (capeName == null) {
            return CapeManager.DEFAULT_CAPE_TEXTURE;
        }

        if (gifElytraTextures.containsKey(capeName)) {
            List<Identifier> frames = gifElytraTextures.get(capeName);
            int frameIndex = currentFrame.get(playerUUID);
            return frames.get(frameIndex);
        } else {
            return staticElytraTextures.getOrDefault(capeName, CapeManager.DEFAULT_CAPE_TEXTURE);
        }
    }

    public void cycleCapeTextures() {
        for (UUID playerUUID : playerCapes.keySet()) {
            String capeName = playerCapes.get(playerUUID);

            if (gifCapeTextures.containsKey(capeName)) {
                List<Identifier> frames = gifCapeTextures.get(capeName);
                int frameIndex = (currentFrame.get(playerUUID) + 1) % frames.size();
                currentFrame.put(playerUUID, frameIndex);
            }else if (gifElytraTextures.containsKey(capeName)) {
                List<Identifier> frames = gifElytraTextures.get(capeName);
                int frameIndex = (currentFrame.get(playerUUID) + 1) % frames.size();
                currentFrame.put(playerUUID, frameIndex);
            }
        }
    }

    public void startCapeAnimation() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                CapeTextureManager.this.cycleCapeTextures();
            }
        },0,100);
    }

    public void stopCapeAnimation() {
        timer.cancel();
    }
}
