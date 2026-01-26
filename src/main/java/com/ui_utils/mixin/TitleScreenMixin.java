package com.ui_utils.mixin;

import com.ui_utils.gui.CustomButtonWidget;
import com.ui_utils.gui.UITheme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ui_utils.UpdateUtils;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "init")
    private void onInitWidgetsNormal(CallbackInfo ci) {
        if (UpdateUtils.isOutdated) {
            if (!UpdateUtils.messageShown) {
                MinecraftClient client = MinecraftClient.getInstance();
                ToastManager toastManager = client.getToastManager();
                Text title = Text.of("UI-Utils " + UpdateUtils.version + " available!");
                Text description = Text.of("Click update button to download");
                SystemToast.add(toastManager, new SystemToast.Type(30000L), title, description);
                UpdateUtils.messageShown = true;
            }

            this.addDrawableChild(CustomButtonWidget.createSmall(4, 4, 80, Text.of("Update v" + UpdateUtils.version), (button) -> UpdateUtils.downloadUpdate()));
        }
    }
}
