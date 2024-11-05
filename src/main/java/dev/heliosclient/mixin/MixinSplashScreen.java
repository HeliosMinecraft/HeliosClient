package dev.heliosclient.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.color.ColorUtils;
import dev.heliosclient.util.render.Renderer2D;
import dev.heliosclient.util.textures.ClearTexture;
import dev.heliosclient.util.textures.ClientTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceReload;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

import static dev.heliosclient.util.textures.ClientTexture.CLIENT_ICON_TEXTURE;

@Mixin(value = SplashOverlay.class, priority = 3001)
public abstract class MixinSplashScreen {

    @Shadow
    @Final
    static Identifier LOGO;
    @Unique
    MutableText subtitleText = Text.literal("Made with " + ColorUtils.red + "♥" + ColorUtils.white + " by HeliosDevelopment").setStyle(Style.EMPTY.withColor(Formatting.WHITE));
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    @Final
    private boolean reloading;
    @Shadow
    private float progress;
    @Shadow
    private long reloadCompleteTime;
    @Shadow
    private long reloadStartTime;
    @Shadow
    @Final
    private ResourceReload reload;

    @Shadow @Final private Consumer<Optional<Throwable>> exceptionHandler;

    @Unique
    private static int withAlpha(int color, int alpha) {
        return color & 16777215 | alpha << 24;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(MinecraftClient client, ResourceReload monitor, Consumer<Optional<Throwable>> exceptionHandler, boolean reloading, CallbackInfo ci) {
        client.getTextureManager().registerTexture(LOGO, new ClearTexture());
        client.getTextureManager().registerTexture(CLIENT_ICON_TEXTURE, new ClientTexture(true));
    }

    @Unique
    private void renderSplashBackground(DrawContext context) {
        int i = context.getScaledWindowWidth();
        int j = context.getScaledWindowHeight();
        int alpha = MathHelper.clamp((int) (getAlpha() * 255),0,255);


        int startColor = ColorHelper.Argb.getArgb(alpha,183, 25, 112);
        int endColor = ColorHelper.Argb.getArgb(alpha,1, 65, 109);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        Renderer2D.drawGradient(context.getMatrices().peek().getPositionMatrix(), 0,0,i,j,startColor,endColor, Renderer2D.Direction.LEFT_RIGHT);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    @Unique
    private float getAlpha() {
        long l = Util.getMeasuringTimeMs();
        float f = this.reloadCompleteTime > -1L ? (float) (l - this.reloadCompleteTime) / 1000.0F : -1.0F;
        float g = this.reloadStartTime > -1L ? (float)(l - this.reloadStartTime) / 500.0F : -1.0F;
        float alpha = 1.0F;
        if(f >= 1.0F) alpha = MathHelper.ceil((1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
        else if(this.reloading) alpha = MathHelper.ceil(MathHelper.clamp(g, 0.15, 1.0) * 255.0F);
        return alpha;
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int i = context.getScaledWindowWidth();
        int j = context.getScaledWindowHeight();
        long l = Util.getMeasuringTimeMs();
        if (this.reloading && this.reloadStartTime == -1L) {
            this.reloadStartTime = l;
        }

        float f = this.reloadCompleteTime > -1L ? (float) (l - this.reloadCompleteTime) / 1000.0F : -1.0F;
        float g = this.reloadStartTime > -1L ? (float) (l - this.reloadStartTime) / 500.0F : -1.0F;
        float alpha;

        if (f >= 1.0F) {
            if (this.client.currentScreen != null) {
                this.client.currentScreen.render(context, 0, 0, delta);
            }
            alpha = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (this.reloading) {
            if (this.client.currentScreen != null && g < 1.0F) {
                this.client.currentScreen.render(context, mouseX, mouseY, delta);
            }
            alpha = MathHelper.clamp(g, 0.0F, 1.0F);
        } else {
            alpha = 1.0F;
        }

        renderSplashBackground(context);

        alpha = MathHelper.clamp(alpha, 0.0F, 1.0F);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        int size = 100;
        float halfScreenWidth = i/2.0f;
        float halfScreenHeight = j/2.0f;

        context.drawTexture(CLIENT_ICON_TEXTURE, (int) halfScreenWidth - (size / 2), (int) halfScreenHeight - (size / 2), size, size, 0, 0, size, size, size, size);

        if (HeliosClient.MC.textRenderer != null) {
            context.drawText(HeliosClient.MC.textRenderer, subtitleText, (int) halfScreenWidth - (HeliosClient.MC.textRenderer.getWidth(subtitleText) / 2) + 5, (int) halfScreenHeight + size/2 + HeliosClient.MC.textRenderer.fontHeight - 4, 0xFFFFFF, true);
        }

        float t = this.reload.getProgress();
        this.progress = MathHelper.clamp(this.progress * 0.95F + t * 0.050000012F, 0.0F, 1.0F);
        //opacity = 1.0F - MathHelper.clamp(f, 0.0F, 1.0F),
        this.renderProgressBar(context, size);

        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        if (f >= 2.0F) {
            this.client.setOverlay(null);
        }

        if (this.reloadCompleteTime == -1L && this.reload.isComplete() && (!this.reloading || g >= 2.0F)) {
            try {
                this.reload.throwException();
                this.exceptionHandler.accept(Optional.empty());
            } catch (Throwable var23) {
                this.exceptionHandler.accept(Optional.of(var23));
            }

            this.reloadCompleteTime = Util.getMeasuringTimeMs();
            if (this.client.currentScreen != null) {
                this.client.currentScreen.init(this.client, context.getScaledWindowWidth(), context.getScaledWindowHeight());
            }
        }
        ci.cancel();
    }

    @Unique
    private void renderProgressBar(DrawContext drawContext, int logo_size) {
        int width = drawContext.getScaledWindowWidth();
        int height = drawContext.getScaledWindowHeight();

        Renderer2D.setDrawContext(drawContext);

        float roundX = (float) (width/2.0f - (logo_size / 2.0) - (width * 0.2f));
        float roundY = (float) (height/2.0f - (logo_size / 2.0) + logo_size + 18);
        float backgroundWidth = (width/2.0f - roundX) * 2;

        float sliderWidth = MathHelper.ceil(backgroundWidth * this.progress);

        if(backgroundWidth <= 0 || sliderWidth <= 0){
            return;
        }

       Renderer2D.drawRoundedRectangle(drawContext.getMatrices().peek().getPositionMatrix(),roundX, roundY,  backgroundWidth,20,5, Color.ORANGE.getRGB());
       int sliderColor = client.options.getMonochromeLogo().getValue() ? Color.BLACK.getRGB() : Color.WHITE.getRGB();
       Renderer2D.drawRoundedRectangleWithShadow(drawContext.getMatrices(), roundX + 3, roundY + 2, sliderWidth - 6, 15.4f,5,25, sliderColor);


        if (HeliosClient.MC.textRenderer == null)
            return;

        int progressPercentage = Math.round(this.progress * 100);

        String progressText = progressPercentage + "%";

        int textX = (int) (width/2.0f - HeliosClient.MC.textRenderer.getWidth(progressText)/2.0f);
        int textY = (int) (roundY + 20 + HeliosClient.MC.textRenderer.fontHeight);


        drawContext.drawText(HeliosClient.MC.textRenderer, progressText, textX, textY, 0xFFFFFF, true);
    }
}