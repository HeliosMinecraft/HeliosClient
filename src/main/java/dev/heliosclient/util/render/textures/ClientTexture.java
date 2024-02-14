package dev.heliosclient.util.render.textures;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.mixin.MixinSplashScreen;
import dev.heliosclient.util.fontutils.FontLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemNotFoundException;

public class ClientTexture extends ResourceTexture {
    public static final Identifier CLIENT_LOGO_TEXTURE = new Identifier(HeliosClient.MODID, "splashscreen/client_splash.png");

    public ClientTexture() {
        super(CLIENT_LOGO_TEXTURE);
    }

    @Override
    protected TextureData loadTextureData(ResourceManager resourceManager) {
        try (InputStream inputStream = FontLoader.class.getResourceAsStream("/assets/heliosclient/splashscreen/client_splash.png")) {
            if(inputStream == null){
                return new ResourceTexture.TextureData(new FileNotFoundException(CLIENT_LOGO_TEXTURE.toString()));
            }
                ResourceTexture.TextureData textureData;
                try {
                    textureData = new ResourceTexture.TextureData(new TextureResourceMetadata(true, true), NativeImage.read(inputStream));
                } catch (Throwable throwable) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable var7) {
                            throwable.addSuppressed(var7);
                        }
                    }

                    throw throwable;
                }

                if (inputStream != null) {
                    inputStream.close();
                }

                return textureData;
            } catch (IOException exception) {
                return new ResourceTexture.TextureData(exception);
            }
    }
}