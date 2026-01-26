package com.ui_utils.mixin;

import com.ui_utils.gui.CustomButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ui_utils.SharedVariables;

@Mixin(SleepingChatScreen.class)
public class SleepingChatScreenMixin extends Screen {
    protected SleepingChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        if (SharedVariables.enabled) {
            addDrawableChild(CustomButtonWidget.create(4, 4, 95, Text.of("Client wake"), (button) -> {
                if (this.client != null && this.client.player != null) {
                    this.client.player.wakeUp();
                    this.client.setScreen(null);
                }
            }));
        }
    }
}
