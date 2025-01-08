package dev.heliosclient.util.textures;

import dev.heliosclient.HeliosClient;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.util.Identifier;

public class ClientTexture extends ResourceTexture {
    public static final Identifier CLIENT_LOGO_TEXTURE = Identifier.of(HeliosClient.MODID,"splashscreen/client_splash.png");
    public static final Identifier CLIENT_ICON_TEXTURE = Identifier.of(HeliosClient.MODID,"icon.png");

    public ClientTexture(boolean icon) {
        super(icon ? CLIENT_ICON_TEXTURE : CLIENT_LOGO_TEXTURE);
    }
    /*
    @Override
    public TextureContents loadContents(ResourceManager resourceManager) {
        try (InputStream inputStream = resourceManager.open(getId().t)) {
            if (inputStream == null) {
                return new TextureContents(new FileNotFoundException(location.toString()));
            }
            TextureContents textureData;
            try {
                textureData = new TextureContents(new TextureResourceMetadata(true, true), NativeImage.read(inputStream));
            } catch (Throwable throwable) {
                try {
                    inputStream.close();
                } catch (Throwable var7) {
                    throwable.addSuppressed(var7);
                }

                throw throwable;
            }

            inputStream.close();

            return textureData;
        } catch (IOException exception) {
            return new TextureContents(exception);
        }
    }
    
     */
}