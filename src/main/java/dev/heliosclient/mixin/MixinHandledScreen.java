package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.misc.InventoryTweaks;
import dev.heliosclient.system.HeliosExecutor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.heliosclient.util.player.InventoryUtils.moveItemQuickMove;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {
    @Shadow
    protected int x;
    @Shadow
    protected int y;
    @Shadow
    @Final
    protected T handler;

    public MixinHandledScreen(Text title) {
        super(title);
    }

    @Shadow
    public abstract void close();

    @Shadow
    public abstract T getScreenHandler();

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        InventoryTweaks invTweaks = ModuleManager.get(InventoryTweaks.class);

    }
}