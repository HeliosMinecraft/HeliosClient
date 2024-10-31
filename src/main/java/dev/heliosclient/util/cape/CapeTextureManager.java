package dev.heliosclient.util.cape;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.CapeManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CapeTextureManager {
    private final ConcurrentHashMap<String, LinkedList<Identifier>> gifTextures;
    private final ConcurrentHashMap<String, Identifier> staticTextures;
    private final ConcurrentHashMap<UUID, CapeData> playerCapes;

    public CapeTextureManager() {
        this.gifTextures = new ConcurrentHashMap<>();
        this.staticTextures = new ConcurrentHashMap<>();
        this.playerCapes = new ConcurrentHashMap<>();

        this.startCapeAnimation();
    }

    public void registerCapeTextures(InputStream stream, File capeFile, String prefix) throws IOException {
        if (capeFile.getName().toLowerCase().endsWith(".gif")) {
            final GifTextureManager gifTextureManager = new GifTextureManager();
            gifTextureManager.registerGifTextures(capeFile, prefix);
            gifTextures.put(prefix + "_cape", gifTextureManager.getCapeTextureIdentifiers());
            gifTextures.put(prefix + "_elytra", gifTextureManager.getElytraTextureIdentifiers());
            gifTextureManager.discardAll();
        } else {
            NativeImage image = NativeImage.read(stream);
            HeliosClient.MC.execute(() -> {
                Identifier capeIdentifier = HeliosClient.MC.getTextureManager().registerDynamicTexture(prefix + "_cape", new NativeImageBackedTexture(CapeManager.parseCape(image)));
                Identifier elytraIdentifier = HeliosClient.MC.getTextureManager().registerDynamicTexture(prefix + "_elytra", new NativeImageBackedTexture(image));
                staticTextures.put(prefix + "_cape", capeIdentifier);
                staticTextures.put(prefix + "_elytra", elytraIdentifier);
            });
        }
    }

    public void assignCapeToPlayer(UUID playerUUID, String capeName) {
        playerCapes.put(playerUUID, new CapeData(capeName));
    }

    public Identifier getCurrentTexture(UUID playerUUID, boolean isElytra) {
        CapeData capeData = playerCapes.get(playerUUID);
        if (capeData == null) {
            return CapeManager.DEFAULT_CAPE_TEXTURE;
        }


        String textureKey = capeData.capeName + (isElytra ? "_elytra" : "_cape");
        List<Identifier> frames = gifTextures.get(textureKey);

        if (frames != null) {
            return frames.get(capeData.currentFrame % frames.size());
        } else {
            return staticTextures.getOrDefault(textureKey, CapeManager.DEFAULT_CAPE_TEXTURE);
        }
    }

    public void cycleCapeTextures() {
        playerCapes.forEach((uuid, capeData) -> {
            List<Identifier> frames = gifTextures.get(capeData.capeName + "_cape");
            if (frames != null && !frames.isEmpty()) {
                capeData.currentFrame = (capeData.currentFrame + 1) % frames.size();
            }
        });
    }

    public void startCapeAnimation() {
        Thread animationThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                cycleCapeTextures();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        animationThread.setName("CapeAnimationThread");
        animationThread.setDaemon(true);
        animationThread.start();
    }

    private static class CapeData {
        String capeName;
        int currentFrame;

        CapeData(String capeName) {
            this.capeName = capeName;
            this.currentFrame = 0;
        }
    }
}
