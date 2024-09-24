package dev.heliosclient.mixin;

import dev.heliosclient.managers.ModuleManager;
import dev.heliosclient.module.modules.chat.ChatTweaks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChatScreen.class, priority = 100)
public abstract class MixinChatScreen {
    @Shadow
    protected TextFieldWidget chatField;

    @Inject(method = "init", at = @At(value = "RETURN"))
    private void onInit(CallbackInfo info) {
        if (ModuleManager.get(ChatTweaks.class).longerChatBox.value && ModuleManager.get(ChatTweaks.class).isActive()) {
            chatField.setMaxLength(Integer.MAX_VALUE);
        }
    }

    @Inject(method = "normalize", at = @At(value = "HEAD"), cancellable = true)
    private void onNormalizeChatMessage(String chatText, CallbackInfoReturnable<String> cir) {
        if (ModuleManager.get(ChatTweaks.class).longerChatBox.value && ModuleManager.get(ChatTweaks.class).isActive()) {
            cir.setReturnValue(StringUtils.normalizeSpace(chatText.trim()));
        }
    }
}