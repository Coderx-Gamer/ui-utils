package org.uiutils.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiutils.SharedVariables;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    private MultiplayerScreenMixin() {
        super(null);
    }

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        // Create "Bypass Resource Pack" option
        this.addDrawableChild(ButtonWidget.builder(Text.of("Bypass Resource Pack: " + (SharedVariables.bypassResourcePack ? "ON" : "OFF")), (button) -> {
            SharedVariables.bypassResourcePack = !SharedVariables.bypassResourcePack;
            button.setMessage(Text.of("Bypass Resource Pack: " + (SharedVariables.bypassResourcePack ? "ON" : "OFF")));
        }).width(160).position(this.width - 170, this.height - 50).build());

        // Create "Force Deny" option
        this.addDrawableChild(ButtonWidget.builder(Text.of("Force Deny: " + (SharedVariables.resourcePackForceDeny ? "ON" : "OFF")), (button) -> {
            SharedVariables.resourcePackForceDeny = !SharedVariables.resourcePackForceDeny;
            button.setMessage(Text.of("Force Deny: " + (SharedVariables.resourcePackForceDeny ? "ON" : "OFF")));
        }).width(160).position(this.width - 170, this.height - 25).build());
    }
}
