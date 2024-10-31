package dev.heliosclient.util.render.textures;

import dev.heliosclient.HeliosClient;
import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ClientTexture extends ResourceTexture {
    public static final Identifier CLIENT_LOGO_TEXTURE = new Identifier(HeliosClient.MODID, "splashscreen/client_splash.png");
    public static final Identifier CLIENT_ICON_TEXTURE = new Identifier(HeliosClient.MODID, "icon.png");

    public ClientTexture(boolean icon) {
        super(icon ? CLIENT_ICON_TEXTURE : CLIENT_LOGO_TEXTURE);
    }

    @Override
    protected TextureData loadTextureData(ResourceManager resourceManager) {
        try (InputStream inputStream = resourceManager.open(location)) {
            if (inputStream == null) {
                return new ResourceTexture.TextureData(new FileNotFoundException(location.toString()));
            }
            ResourceTexture.TextureData textureData;
            try {
                textureData = new ResourceTexture.TextureData(new TextureResourceMetadata(true, true), NativeImage.read(inputStream));
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
            return new ResourceTexture.TextureData(exception);
        }
    }
}