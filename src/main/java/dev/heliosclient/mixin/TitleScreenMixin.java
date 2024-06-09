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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    public TitleScreenMixin(Text text) {
        super(text);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/realms/gui/screen/RealmsNotificationsScreen;init(Lnet/minecraft/client/MinecraftClient;II)V"), method = "init", index = 2)
    private int adjustRealmsHeight(int height) {
        return height - 51;
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void clientTag(CallbackInfo ci) {
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
