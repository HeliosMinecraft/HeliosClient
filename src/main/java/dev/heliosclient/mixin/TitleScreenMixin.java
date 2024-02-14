package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.ui.HeliosClientInfoScreen;
import dev.heliosclient.ui.altmanager.AltManagerScreen;
import dev.heliosclient.util.render.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Shadow
    @Final
    private boolean doBackgroundFade;

    @Shadow
    private long backgroundFadeStart;

    public TitleScreenMixin() {
        super(null);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/realms/gui/screen/RealmsNotificationsScreen;init(Lnet/minecraft/client/MinecraftClient;II)V"), method = "init", index = 2)
    private int adjustRealmsHeight(int height) {
        return height - 51;
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void clientTag(CallbackInfo ci) {
        float f = this.doBackgroundFade ? (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0F : 1.0F;
        float g = this.doBackgroundFade ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
        int l = MathHelper.ceil(g * 255.0F) << 24;

        this.addDrawableChild(new PressableTextWidget(2, 2, 150, 10, Text.literal(HeliosClient.clientTag + " " + HeliosClient.versionTag), (button) -> {
            if (this.client != null) {
                this.client.setScreen(HeliosClientInfoScreen.INSTANCE);
            }
        }, this.textRenderer));
    }

    @Inject(at = @At("RETURN"), method = "render")
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Renderer2D.setDrawContext(context);
    }

    @Inject(at = @At("RETURN"), method = "init")
    private void altManagerButton(CallbackInfo callbackInfo) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Alt Manager"), this::gotoAltManagerScreen)
                .position(this.width - 102, 0)
                .size(100, 20)
                .build());
    }

    @Unique
    private void gotoAltManagerScreen(ButtonWidget button) {
        HeliosClient.MC.setScreen(AltManagerScreen.INSTANCE);
    }
}
