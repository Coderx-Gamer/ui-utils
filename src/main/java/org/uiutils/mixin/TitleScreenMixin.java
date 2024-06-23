package org.uiutils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiutils.UpdateUtils;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "initWidgetsNormal(II)V")
    private void onInitWidgetsNormal(int Y, int spacingY, CallbackInfo ci) {
        if (UpdateUtils.isOutdated) {
            if (!UpdateUtils.messageShown) {
                MinecraftClient client = MinecraftClient.getInstance();
                ToastManager toastManager = client.getToastManager();
                Text title = Text.of("UI-Utils " + UpdateUtils.version + " is out!");
                Text description = Text.of("Download it from the top left corner!");
                SystemToast.add(toastManager, new SystemToast.Type(30000L), title, description);
                UpdateUtils.messageShown = true;
            }

            Text message = Text.of("Download UI-Utils " + UpdateUtils.version + "!");

            this.addDrawableChild(new TextWidget(40 - 15, 5, textRenderer.getWidth(message), textRenderer.fontHeight, message, textRenderer));

            ButtonWidget downloadUpdateButton = new TexturedButtonWidget(5, 5 - 3,
                    15, 15,
                    new ButtonTextures(
                            Identifier.of("uiutils", "update"),
                            Identifier.of("uiutils", "update_selected")
                    ),
                    (button) -> UpdateUtils.downloadUpdate(),
                    Text.of("Download Update"));
            this.addDrawableChild(downloadUpdateButton);

        }
    }
}
