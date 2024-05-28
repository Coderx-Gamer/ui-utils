package org.uiutils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiutils.SharedVariables;

@Mixin(SignEditScreen.class)
public class SignEditScreenMixin extends Screen {
    protected SignEditScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // called when any sign edit screen is created
    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {

        // register "close without packet" button for SignEditScreen if ui utils is enabled
        if (SharedVariables.enabled) {
            addDrawableChild(ButtonWidget.builder(Text.of("Close without packet"), (button) -> {
                // disables sign editing and closes the current gui without sending a packet
                SharedVariables.shouldEditSign = false;
                mc.setScreen(null);
            }).width(115).position(5, 5).build());
            addDrawableChild(ButtonWidget.builder(Text.of("Disconnect"), (button) -> {
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().getConnection().disconnect(Text.of("Disconnecting (UI-UTILS)"));
                }
            }).width(115).position(5, 35).build());
        }
    }
}
