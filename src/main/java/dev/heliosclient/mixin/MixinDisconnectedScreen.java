package dev.heliosclient.mixin;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.misc.AutoReconnect;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class MixinDisconnectedScreen extends Screen {
    AutoReconnect autoReconnect = ModuleManager.get(AutoReconnect.class);
    double timer = autoReconnect.delay.value * 20;
    ButtonWidget reconnectingButton;
    @Shadow
    @Final
    private DirectionalLayoutWidget grid;

    protected MixinDisconnectedScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;refreshPositions()V"))
    private void addButtons(CallbackInfo ci) {
        if (autoReconnect.lastConnection != null) {

            ButtonWidget reconnectButton = ButtonWidget.builder(Text.of("Reconnect"), button -> tryConnect()).build();
            reconnectingButton = new ButtonWidget.Builder(getMessage(), button -> {
                autoReconnect.toggle();
                reconnectingButton.setMessage(getMessage());
                timer = autoReconnect.delay.value * 20;
            }).build();

            grid.add(reconnectButton);
            grid.add(reconnectingButton);
        }
    }

    @Unique
    private Text getMessage() {
        return autoReconnect.isActive() ? Text.literal("Reconnecting in " + String.format("(%.1fs)", timer / 20)) : Text.literal("Reconnection paused");
    }

    @Override
    public void tick() {
        if (autoReconnect.lastConnection == null) return;

        if (timer <= 0) {
            tryConnect();
        } else if (autoReconnect.isActive()) {
            timer--;
            if (reconnectingButton != null)
                reconnectingButton.setMessage(Text.literal("Reconnecting in " + String.format("(%.1fs)", timer / 20)));
        }
    }

    @Unique
    private void tryConnect() {
        ServerInfo info = autoReconnect.lastConnection;
        ConnectScreen.connect(new TitleScreen(), HeliosClient.MC, ServerAddress.parse(info.address), info, false);
    }
}
