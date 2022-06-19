package org.uiutils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiutils.SharedVariables;

@Mixin(SignEditScreen.class)
public class SignEditScreenMixin extends Screen {
    protected SignEditScreenMixin(Text title) {
        super(title);
    }

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        this.addDrawableChild(new ButtonWidget(5, 5, 160, 20, Text.of("close without packet"), (button) -> {
            SharedVariables.shouldEditSign = false;
            mc.setScreen(null);
        }));
    }
}
