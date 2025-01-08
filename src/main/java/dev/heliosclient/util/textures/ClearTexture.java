package dev.heliosclient.util.textures;

import dev.heliosclient.HeliosClient;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.util.Identifier;

public class ClearTexture extends ResourceTexture {
    private static final Identifier CLEAR_TEXTURE = Identifier.of(HeliosClient.MODID, "splashscreen/clear.png");

    public ClearTexture() {
        super(CLEAR_TEXTURE);
    }

    /*
    protected TextureData loadTextureData(ResourceManager resourceManager) {
        try {
            InputStream input =  resourceManager.open(location);
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

     */

}