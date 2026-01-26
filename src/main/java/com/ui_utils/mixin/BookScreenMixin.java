package com.ui_utils.mixin;

import com.ui_utils.gui.ChatTextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ui_utils.MainClient;
import com.ui_utils.SharedVariables;

@Mixin(BookScreen.class)
public class BookScreenMixin extends Screen {
    protected BookScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        if (SharedVariables.enabled) {
            MainClient.createWidgets(mc, this);

            ChatTextFieldWidget chatField = new ChatTextFieldWidget(textRenderer, 6, this.height - 18, 140, 12, Text.of("Chat..."));
            chatField.setText("");
            chatField.setMaxLength(255);
            this.addDrawableChild(chatField);
        }
    }
}
