package com.ui_utils.mixin;

import com.ui_utils.features.CommandSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ui_utils.SharedVariables;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(at = @At("HEAD"), method = "sendMessage", cancellable = true)
    public void sendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        if (chatText.startsWith(SharedVariables.commandPrefix)) {
            String command = chatText.substring(SharedVariables.commandPrefix.length());
            mc.inGameHud.getChatHud().addToMessageHistory(chatText);
            
            String result = CommandSystem.execute(command);
            if (mc.player != null && !result.isEmpty()) {
                for (String line : result.split("\n")) {
                    mc.player.sendMessage(Text.of(line), false);
                }
            }
            
            mc.setScreen(null);
            ci.cancel();
        }
    }
}
