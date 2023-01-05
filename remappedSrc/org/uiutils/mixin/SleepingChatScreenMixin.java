package org.uiutils.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepingChatScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SleepingChatScreen.class)
public class SleepingChatScreenMixin extends Screen {
    protected SleepingChatScreenMixin(Text title) {
        super(title);
    }


    // called when SleepingChatScreen is created
    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {

        // register "client wake up" button for SleepingChatScreen
        addDrawableChild(new ButtonWidget(5, 5, 160, 20, Text.of("Client wake up"), (button) -> {

            // wakes the player up client-side
            client.player.wakeUp();
            client.setScreen(null);
        }));
    }
}
