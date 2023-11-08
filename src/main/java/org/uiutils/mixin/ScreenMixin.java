package org.uiutils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.LecternScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiutils.MainClient;
import org.uiutils.SharedVariables;
import org.uiutils.mixin.accessor.ScreenAccessor;

import java.util.regex.Pattern;

@SuppressWarnings("all")
@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow
    public abstract <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement);

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private TextFieldWidget addressField;
    private boolean initialized = false;

    // inject at the end of the render method (if instanceof LecternScreen)
    @Inject(at = @At("TAIL"), method = "init(Lnet/minecraft/client/MinecraftClient;II)V")
    public void init(MinecraftClient client, int width, int height, CallbackInfo ci) {
        // check if the current gui is a lectern gui and if ui-utils is enabled
        if (mc.currentScreen instanceof LecternScreen screen && SharedVariables.enabled) {
            // setup widgets
            if (/*!this.initialized*/ true) {
                // check if the current gui is a lectern gui and ui-utils is enabled
                TextRenderer textRenderer = ((ScreenAccessor) this).getTextRenderer();
                MainClient.createWidgets(mc, screen);

                // create chat box
                this.addressField = new TextFieldWidget(textRenderer, 5, 245, 160, 20, Text.of("Chat ...")) {
                    @Override
                    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                        if (keyCode == GLFW.GLFW_KEY_ENTER) {
                            if (this.getText().equals("^toggleuiutils")) {
                                SharedVariables.enabled = !SharedVariables.enabled;
                                if (mc.player != null) {
                                    mc.player.sendMessage(Text.of("UI-Utils is now " + (SharedVariables.enabled ? "enabled" : "disabled") + "."));
                                }
                                return false;
                            }

                            if (mc.getNetworkHandler() != null) {
                                if (this.getText().startsWith("/")) {
                                    mc.getNetworkHandler().sendChatCommand(this.getText().replaceFirst(Pattern.quote("/"), ""));
                                } else {
                                    mc.getNetworkHandler().sendChatMessage(this.getText());
                                }
                            } else {
                                MainClient.LOGGER.warn("Minecraft network handler (mc.getNetworkHandler()) was null while trying to send chat message from UI Utils.");
                            }

                            this.setText("");
                        }
                        return super.keyPressed(keyCode, scanCode, modifiers);
                    }
                };
                this.addressField.setText("");
                this.addressField.setMaxLength(255);

                this.addDrawableChild(this.addressField);
                this.initialized = true;
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // display sync id, revision, and credit if ui utils is enabled
        if (SharedVariables.enabled && mc.player != null && mc.currentScreen instanceof LecternScreen) {
            MainClient.createText(mc, context, ((ScreenAccessor) this).getTextRenderer());
        }
    }
}
