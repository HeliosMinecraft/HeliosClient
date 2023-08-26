    package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.ui.HeliosClientInfoScreen;
import dev.heliosclient.ui.altmanager.AltManagerScreen;

import net.minecraft.client.gui.screen.option.CreditsAndAttributionScreen;
import net.minecraft.client.gui.widget.PressableTextWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen 
{ 
    @Shadow
    @Final
    private boolean doBackgroundFade;

    @Shadow
    @Final
    private long backgroundFadeStart;

    public TitleScreenMixin() 
    {
        super(null);
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void clientTag(DrawContext drawContext, int mouseX, int mouseY, float delta, CallbackInfo info) 
    {
        float f = this.doBackgroundFade ? (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0F : 1.0F;
        float g = this.doBackgroundFade ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
        int l = MathHelper.ceil(g * 255.0F) << 24;

        //drawContext.drawTextWithShadow(this.textRenderer, HeliosClient.clientTag + " " + HeliosClient.versionTag, 2, 2, 16777215 | l);

        this.addDrawableChild(new PressableTextWidget(2, 2, 150, 10, Text.literal(HeliosClient.clientTag + " " + HeliosClient.versionTag), (button) -> {
            this.client.setScreen(HeliosClientInfoScreen.INSTANCE);
        }, this.textRenderer));
    }

    @Inject(at = @At("TAIL"), method = "init")
	private void altManagerButton(CallbackInfo callbackInfo) 
    {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Alt Manager"), this::gotoAltManagerScreen)
            .position(this.width - 102, 2)
            .size(100, 20)
            .build());
	}

    private void gotoAltManagerScreen(ButtonWidget button)
    {
        MinecraftClient.getInstance().setScreen(AltManagerScreen.INSTANCE);
    }
}
