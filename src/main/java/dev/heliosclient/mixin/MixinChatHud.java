package dev.heliosclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.heliosclient.event.events.player.ChatMessageEvent;
import dev.heliosclient.managers.EventManager;
import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.chat.ChatTweaks;
import dev.heliosclient.module.modules.render.GUI;
import dev.heliosclient.ui.clickgui.gui.PolygonMeshPatternRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(ChatHud.class)
public abstract class MixinChatHud {
    @Unique
    private final ArrayList<Long> messageTimestamps = new ArrayList<>();
    @Unique
    boolean ignoreAddMessage = false;
    @Shadow
    @Final
    private MinecraftClient client;
    @Unique
    private float chatXMovement;
    @Unique
    private int chatLineIndex;

    @Shadow
    protected abstract void logChatMessage(Text message, @Nullable MessageIndicator indicator);

    @Shadow
    protected abstract void addMessage(Text message, @Nullable MessageSignatureData signature, int ticks, @Nullable MessageIndicator indicator, boolean refresh);

    @Shadow
    public abstract int getWidth();

    @Shadow protected abstract boolean isChatFocused();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;addedTime()I"))
    public void getChatLineIndex(CallbackInfo ci, @Local(ordinal = 13) int chatLineIndex) {
        this.chatLineIndex = chatLineIndex;
    }
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;isChatHidden()Z",shift = At.Shift.AFTER))
    public void onRender$GUICoolVisuals(DrawContext context, int currentTick, int mouseX, int mouseY,CallbackInfo ci) {
        if(this.isChatFocused() && GUI.coolVisualsChatHud()){
            PolygonMeshPatternRenderer.INSTANCE.render(context.getMatrices(),mouseX,mouseY);
        }
    }

    @Unique
    private void calculateXOffset() {
        //Starts from outside the visible screen.
        if (messageTimestamps.size() > chatLineIndex) {
            float maxDisplacement = -getWidth();
            long timestamp = messageTimestamps.get(chatLineIndex);
            long timeAlive = System.currentTimeMillis() - timestamp;
            double maxTime = ModuleManager.get(ChatTweaks.class).time.value; // ms

            //Gets the ratio between 0.0 and 1.0f
            float ratio = (float) Math.min(1.0f, ((float) timeAlive / maxTime));

            // Clamps between -getWidth() to 0
            chatXMovement = maxDisplacement + Math.abs(ratio * maxDisplacement);
        } else {
            chatXMovement = 0;
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Text message, MessageSignatureData signature, MessageIndicator indicator, CallbackInfo ci) {
        if (ignoreAddMessage)
            return;

        ChatMessageEvent event = new ChatMessageEvent(message, indicator, signature);
        EventManager.postEvent(event);
        if (event.isCanceled()) {
            ci.cancel();
            if (!event.getMessage().getString().equals(message.getString())) {
                ignoreAddMessage = true;
                logChatMessage(event.getMessage(), indicator);
                addMessage(event.getMessage(), signature, client.inGameHud.getTicks(), indicator, false);
                ignoreAddMessage = false;
            }
        } else {
            ignoreAddMessage = false;
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
        messageTimestamps.add(0, System.currentTimeMillis());
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"))
    private void onRenderBeforeDrawContext(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (ModuleManager.get(ChatTweaks.class).slideInAnimation.value && ModuleManager.get(ChatTweaks.class).isActive()) {
            calculateXOffset();
            context.getMatrices().translate(chatXMovement, 0, 0);
        }
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;indicator()Lnet/minecraft/client/gui/hud/MessageIndicator;"))
    private MessageIndicator onAddMessage(MessageIndicator original) {
        if (ModuleManager.get(ChatTweaks.class).removeMessageIndicator.value && ModuleManager.get(ChatTweaks.class).isActive()) {
            return null;
        }
        return original;
    }

    //modify max lengths for messages and visible messages
    @ModifyExpressionValue(method = "addToMessageHistory", at = @At(value = "CONSTANT", args = "intValue=100"))
    private int maxLength(int size) {
        if (keepHistory())
            return Integer.MAX_VALUE - size;

        return size;
    }

    @ModifyExpressionValue(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "CONSTANT", args = "intValue=100"))
    private int maxLengthIncrease(int size) {
        if (keepHistory())
            return Integer.MAX_VALUE - size;

        return size;
    }

    @Unique
    private boolean keepHistory() {
        return ModuleManager.get(ChatTweaks.class).keepHistory.value && ModuleManager.get(ChatTweaks.class).isActive();
    }
}
