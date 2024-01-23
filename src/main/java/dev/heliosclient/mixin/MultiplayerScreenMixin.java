package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.ui.altmanager.AltManagerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void altManagerButton(CallbackInfo callbackInfo) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Alt Manager"), this::gotoAltManagerScreen)
                .position(this.width - 102, 2)
                .size(100, 20)
                .build());
    }

    @Unique
    private void gotoAltManagerScreen(ButtonWidget button) {
        HeliosClient.MC.setScreen(AltManagerScreen.INSTANCE);
    }
}
