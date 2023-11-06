package org.uiutils.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiutils.SharedVariables;

@Mixin(SleepingChatScreen.class)
public class SleepingChatScreenMixin extends Screen {
    protected SleepingChatScreenMixin(Text title) {
        super(title);
    }

    // called when SleepingChatScreen is created
    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        // register "client wake up" button for SleepingChatScreen if ui utils is enabled
        if (SharedVariables.enabled) {
            addDrawableChild(ButtonWidget.builder(Text.of("Client wake up"), (button) -> {
                // wakes the player up client-side
                if (this.client != null && this.client.player != null) {
                    this.client.player.wakeUp();
                    this.client.setScreen(null);
                }
            }).width(115).position(5, 5).build());
        }
    }
}
