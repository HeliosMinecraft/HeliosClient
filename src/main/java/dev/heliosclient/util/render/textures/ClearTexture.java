package dev.heliosclient.util.render.textures;

import dev.heliosclient.HeliosClient;
import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;

public class ClearTexture extends ResourceTexture {
    private static final Identifier CLEAR_TEXTURE = new Identifier(HeliosClient.MODID, "splashscreen/clear.png");

    public ClearTexture(Identifier location) {
        super(location);
    }
    public ClearTexture() {
        super(CLEAR_TEXTURE);
    }

    protected TextureData loadTextureData(ResourceManager resourceManager) {
        try {
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/heliosclient/splashscreen/clear.png");
            TextureData texture = null;

            if (input != null) {
                try {
                    texture = new TextureData(new TextureResourceMetadata(true, true), NativeImage.read(input));
                } finally {
                    input.close();
                }

            }

            return texture;
        } catch (IOException exception) {
            return new TextureData(exception);
        }
    }

}