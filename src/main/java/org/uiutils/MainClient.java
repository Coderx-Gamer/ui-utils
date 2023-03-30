package org.uiutils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.commons.io.FileUtils;
import org.lwjgl.glfw.GLFW;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class MainClient implements ClientModInitializer {
    public static KeyBinding restoreScreenKey;

    @Override
    public void onInitializeClient() {
        try {
            FileUtils.copyURLToFile(new URL("https://github.com/MrBreakNFix/mrbreaknfix.github.io/raw/main/api/sounds/sniff.wav"), new File("sniff.wav"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // register "restore screen" key
        restoreScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Restore Screen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "UI Utils"));

        // register event for END_CLIENT_TICK
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {

            // detect if the "restore screen" keybinding is pressed
            while (restoreScreenKey.wasPressed()) {
                if (SharedVariables.storedScreen != null && SharedVariables.storedScreenHandler != null) {
                    client.setScreen(SharedVariables.storedScreen);
                    client.player.currentScreenHandler = SharedVariables.storedScreenHandler;
                }
            }
        });

        // set java.awt.headless to false (allows for jframe guis to be used)
        System.setProperty("java.awt.headless", "false");

        try {

            // set uimanager to system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void renderHandledScreen(MinecraftClient mc, TextRenderer textRenderer, MatrixStack matrices) {

        // display the current gui's sync id and revision
        textRenderer.draw(matrices, "Sync Id: " + mc.player.currentScreenHandler.syncId, 200, 5, Color.WHITE.getRGB());
        textRenderer.draw(matrices, "Revision: " + mc.player.currentScreenHandler.getRevision(), 200, 35, Color.WHITE.getRGB());
    }

    public static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static SlotActionType stringToSlotActionType(String string) {

        // converts a string to SlotActionType
        switch (string) {
            case "PICKUP" -> {
                return SlotActionType.PICKUP;
            }
            case "QUICK_MOVE" -> {
                return SlotActionType.QUICK_MOVE;
            }
            case "SWAP" -> {
                return SlotActionType.SWAP;
            }
            case "CLONE" -> {
                return SlotActionType.CLONE;
            }
            case "THROW" -> {
                return SlotActionType.THROW;
            }
            case "QUICK_CRAFT" -> {
                return SlotActionType.QUICK_CRAFT;
            }
            case "PICKUP_ALL" -> {
                return SlotActionType.PICKUP_ALL;
            }
            default -> {
                return null;
            }
        }
    }

    public static void queueTask(Runnable runnable, long delay) {

        // queues a task
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                MinecraftClient.getInstance().send(runnable);
                timer.purge();
                timer.cancel();
            }
        };
        timer.schedule(task, delay);
    }
}
