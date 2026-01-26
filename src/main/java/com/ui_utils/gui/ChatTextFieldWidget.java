package com.ui_utils.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import com.ui_utils.SharedVariables;

import java.util.regex.Pattern;

public class ChatTextFieldWidget extends CustomTextFieldWidget {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public ChatTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            String toggleCmd = SharedVariables.commandPrefix + "toggleuiutils";
            if (this.getText().equalsIgnoreCase(toggleCmd)) {
                SharedVariables.enabled = !SharedVariables.enabled;
                if (mc.player != null) {
                    mc.player.sendMessage(Text.of("§7UI-Utils: " + (SharedVariables.enabled ? "ON" : "OFF")), false);
                }
                this.setText("");
                return false;
            }
            if (mc.getNetworkHandler() != null) {
                if (this.getText().startsWith("/")) {
                    mc.getNetworkHandler().sendChatCommand(this.getText().replaceFirst(Pattern.quote("/"), ""));
                } else {
                    mc.getNetworkHandler().sendChatMessage(this.getText());
                }
            }
            this.setText("");
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
