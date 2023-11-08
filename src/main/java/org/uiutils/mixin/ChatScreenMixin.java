package org.uiutils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.uiutils.MainClient;
import org.uiutils.SharedVariables;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(at = @At("HEAD"), method = "sendMessage", cancellable = true)
    public void sendMessage(String chatText, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {
        if (chatText.equals("^toggleuiutils")) {
            SharedVariables.enabled = !SharedVariables.enabled;
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.of("UI-Utils is now " + (SharedVariables.enabled ? "enabled" : "disabled") + "."));
            } else {
                MainClient.LOGGER.warn("Minecraft player was nulling while enabling / disabling UI Utils.");
            }
            MinecraftClient.getInstance().inGameHud.getChatHud().addToMessageHistory(chatText);
            MinecraftClient.getInstance().setScreen(null);
            cir.setReturnValue(false);
        }
    }
}
