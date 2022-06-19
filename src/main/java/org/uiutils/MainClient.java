package org.uiutils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MainClient implements ClientModInitializer {
    public static KeyBinding restoreScreenKey;

    @Override
    public void onInitializeClient() {
        restoreScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Restore Screen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_L, "UI Utils"));

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            while (restoreScreenKey.wasPressed()) {
                if (SharedVariables.storedScreen != null && SharedVariables.storedScreenHandler != null) {
                    client.setScreen(SharedVariables.storedScreen);
                    client.player.currentScreenHandler = SharedVariables.storedScreenHandler;
                }
            }
        });
    }
}
