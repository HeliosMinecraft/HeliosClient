package dev.heliosclient.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.system.ClearTexture;
import dev.heliosclient.system.ClientTexture;
import dev.heliosclient.util.ColorUtils;
import dev.heliosclient.util.Renderer2D;
import dev.heliosclient.util.fontutils.FontRenderers;
import me.x150.renderer.font.FontRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import static dev.heliosclient.system.ClientTexture.CLIENT_LOGO_TEXTURE;

@Mixin(value = SplashOverlay.class, priority = 3000)
public abstract class MixinSplashScreen {

    @Shadow
    @Final
    static Identifier LOGO;
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
    @Unique
    private static final Identifier CLEAR_TEXTURE = new Identifier(HeliosClient.MODID, "splashscreen/clear.png");
    MutableText subtitleText = Text.literal("Made with " + ColorUtils.red + "♥" + ColorUtils.white + " by HeliosDevelopment").setStyle(Style.EMPTY.withColor(Formatting.WHITE));



    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(MinecraftClient client, ResourceReload monitor, Consumer<Optional<Throwable>> exceptionHandler, boolean reloading, CallbackInfo ci) {
      client.getTextureManager().registerTexture(LOGO,  new ClearTexture(CLEAR_TEXTURE));
      client.getTextureManager().registerTexture(CLIENT_LOGO_TEXTURE, new ClientTexture());
    }

    @Unique
    private static int withAlpha(int color, int alpha) {
        return color & 16777215 | alpha << 24;
    }
  @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;getScaledWindowWidth()I", shift = At.Shift.BEFORE, ordinal = 2))
    private void renderSplashBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int i = context.getScaledWindowWidth();
        int j = context.getScaledWindowHeight();
        float s = getS();
            RenderSystem.enableBlend();
            RenderSystem.blendEquation(32774);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, s);
            context.fill(RenderLayer.getGuiOverlay(), 0, 0, i, j, ColorHelper.Argb.getArgb(255, 0, 0, 0));
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
    }



    @Unique
    private float getS() {
        float f = this.reloadCompleteTime > -1L ? (float)(Util.getMeasuringTimeMs() - this.reloadCompleteTime) / 1000.0F : -1.0F;
        float g = this.reloadStartTime> -1L ? (float)(Util.getMeasuringTimeMs() - this.reloadStartTime) / 500.0F : -1.0F;
        float s;
        if (f >= 1.0F) s = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
        else if (reloading) s = MathHelper.clamp(g, 0.0F, 1.0F);
        else s = 1.0F;
        return s;
    }

    @Inject(at = @At("RETURN"), method = "render", cancellable = true)
    public void render2(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int i = context.getScaledWindowWidth();
        int j = context.getScaledWindowHeight();
        long l = Util.getMeasuringTimeMs();
        if (this.reloading && this.reloadStartTime == -1L) {
            this.reloadStartTime = l;
        }

        float f = this.reloadCompleteTime > -1L ? (float)(l - this.reloadCompleteTime) / 1000.0F : -1.0F;
        float g = this.reloadStartTime > -1L ? (float)(l - this.reloadStartTime) / 500.0F : -1.0F;
        int k;
        if (f >= 1.0F) {
            if (this.client.currentScreen != null) {
                this.client.currentScreen.render(context, 0, 0, delta);
            }

            k = MathHelper.ceil((1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
            context.fill(RenderLayer.getGuiOverlay(), 0, 0, i, j, withAlpha(ColorHelper.Argb.getArgb(255, 0, 0, 0), k));
        } else if (this.reloading) {
            if (this.client.currentScreen != null && g < 1.0F) {
                this.client.currentScreen.render(context, mouseX, mouseY, delta);
            }

            k = MathHelper.ceil(MathHelper.clamp((double)g, 0.15, 1.0) * 255.0);
            context.fill(RenderLayer.getGuiOverlay(), 0, 0, i, j, withAlpha(ColorHelper.Argb.getArgb(255, 0, 0, 0), k));
        } else {
            k = ColorHelper.Argb.getArgb(255, 0, 0, 0);
            float m = (float)(k >> 16 & 255) / 255.0F;
            float n = (float)(k >> 8 & 255) / 255.0F;
            float o = (float)(k & 255) / 255.0F;
            GlStateManager._clearColor(m, n, o, 1.0F);
            GlStateManager._clear(16384, MinecraftClient.IS_SYSTEM_MAC);
        }

            double d = Math.min((double)this.client.getWindow().getScaledWidth() * 0.75D, this.client.getWindow().getScaledHeight()) * 0.25D;
            float verticalPosition = 0.0F;

            float s = 1.0f;

            if (f >= 1.0F) s = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
            else if (this.reloading) s = MathHelper.clamp((this.reloadStartTime > -1L ? (float)(Util.getMeasuringTimeMs() - this.reloadStartTime) / 500.0F : -1.0F), 0.0F, 1.0F);

            int w = (int)(d * 4);
        if (this.reloading) {
            float elapsedTime = (float)(Util.getMeasuringTimeMs() - this.reloadStartTime) / 1000.0F;

            verticalPosition = (float) MathHelper.lerp(elapsedTime / 4.0F, d, 0);
        }

            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, s);

            context.drawTexture(CLIENT_LOGO_TEXTURE,(int)(this.client.getWindow().getScaledWidth() * 0.5D) - (w / 2) - 12, (int)(d - verticalPosition), w + 5, w/2, 0, 0, 1024, 512, 1024, 512);

          if(HeliosClient.MC.textRenderer != null) {
              context.drawText(HeliosClient.MC.textRenderer, subtitleText, (int) (this.client.getWindow().getScaledWidth() * 0.5D) - HeliosClient.MC.textRenderer.getWidth(subtitleText) / 2, w / 2 + HeliosClient.MC.textRenderer.fontHeight + 17, 0xFFFFFF, true);
          }
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();

        float t = this.reload.getProgress();
        this.progress = MathHelper.clamp(this.progress * 0.95F + t * 0.050000012F, 0.0F, 1.0F);
        if (f < 1.0F) {
            this.renderProgressBar(context, 1.0F - MathHelper.clamp(f, 0.0F, 1.0F));
        }

        if (f >= 2.0F) {
           this.client.setOverlay((Overlay)null);
        }

        if (this.reloadCompleteTime == -1L && this.reload.isComplete() && (!this.reloading || g >= 2.0F)) {
            try {
                this.reload.throwException();
              //  this.exceptionHandler.accept(Optional.empty());
            } catch (Throwable var23) {
               // this.exceptionHandler.accept(Optional.of(var23));
            }

            this.reloadCompleteTime = Util.getMeasuringTimeMs();
            if (this.client.currentScreen != null) {
                this.client.currentScreen.init(this.client, context.getScaledWindowWidth(), context.getScaledWindowHeight());
            }
        }


       ci.cancel();
    }
    @Unique
    private void renderProgressBar(DrawContext drawContext, float opacity) {
        int width = drawContext.getScaledWindowWidth();
        int height = drawContext.getScaledWindowHeight();

        int progressBarHeight = 2;

        int minX = 0;
        int minY = height - progressBarHeight;

        int filledWidth = MathHelper.ceil((float)width * this.progress);

        int j = Math.round(opacity * 255.0F);

        if(filledWidth <= 0 || height <= 0 || minY <= 0){
            return;
        }

        Renderer2D.setDrawContext(drawContext);
        Renderer2D.drawRectangleWithShadow(drawContext.getMatrices(),minX, minY,filledWidth, height,withAlpha(ColorUtils.getRainbowColor().getRGB(),j),3);

        if(HeliosClient.MC.textRenderer == null)
            return;

        int progressPercentage = Math.round(this.progress * 100);

        String progressText = progressPercentage + "%";

        int textX = width - HeliosClient.MC.textRenderer.getWidth(progressText) - 5;
        int textY = minY -HeliosClient.MC.textRenderer.fontHeight;


        drawContext.drawText(HeliosClient.MC.textRenderer,progressText, textX, textY, 0xFFFFFF, true);
    }

}
