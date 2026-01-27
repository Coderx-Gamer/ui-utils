package com.ui_utils.mixin;

import com.ui_utils.gui.ChatTextFieldWidget;
import com.ui_utils.gui.CustomTextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.LecternScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ui_utils.MainClient;
import com.ui_utils.SharedVariables;
import com.ui_utils.mixin.accessor.ScreenAccessor;

@SuppressWarnings("all")
@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow
    public abstract <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement);

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private CustomTextFieldWidget chatField;
    private boolean initialized = false;

    @Inject(at = @At("TAIL"), method = "init(Lnet/minecraft/client/MinecraftClient;II)V")
    public void init(MinecraftClient client, int width, int height, CallbackInfo ci) {
        if (mc.currentScreen instanceof LecternScreen screen && SharedVariables.enabled) {
            TextRenderer textRenderer = ((ScreenAccessor) this).getTextRenderer();
            MainClient.createWidgets(mc, screen);

            this.chatField = new ChatTextFieldWidget(textRenderer, 6, height - 18, 140, 12, Text.of("Chat..."));
            this.chatField.setText("");
            this.chatField.setMaxLength(255);
            this.addDrawableChild(this.chatField);
            this.initialized = true;
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (SharedVariables.enabled && mc.player != null && mc.currentScreen instanceof LecternScreen) {
            MainClient.createText(mc, context, ((ScreenAccessor) this).getTextRenderer());
        }
    }
}
