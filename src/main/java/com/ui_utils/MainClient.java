package com.ui_utils;

import com.google.common.collect.ImmutableList;
import com.ui_utils.gui.CustomButtonWidget;
import com.ui_utils.gui.UITheme;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ui_utils.mixin.accessor.ClientConnectionAccessor;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class MainClient implements ClientModInitializer {
    public static Font monospace;
    public static Color darkWhite;

    public static KeyBinding restoreScreenKey;

    public static Logger LOGGER = LoggerFactory.getLogger("ui-utils");
    public static MinecraftClient mc = MinecraftClient.getInstance();
    @Override
    public void onInitializeClient() {
        UpdateUtils.checkForUpdates();

        restoreScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Restore Screen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "UI Utils"));

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            com.ui_utils.features.PluginScanner.onTick();
            
            while (restoreScreenKey.wasPressed()) {
                if (SharedVariables.storedScreen != null && SharedVariables.storedScreenHandler != null && client.player != null) {
                    client.setScreen(SharedVariables.storedScreen);
                    client.player.currentScreenHandler = SharedVariables.storedScreenHandler;
                }
            }
        });

        if (!MinecraftClient.IS_SYSTEM_MAC) {
            System.setProperty("java.awt.headless", "false");
            monospace = new Font(Font.MONOSPACED, Font.PLAIN, 10);
            darkWhite = new Color(220, 220, 220);
        }
    }

    @SuppressWarnings("all")
    public static void createText(MinecraftClient mc, DrawContext context, TextRenderer textRenderer) {
        UITheme.drawPanel(context, 153, 4, 56, 24);
        context.drawText(textRenderer, "Sync: " + mc.player.currentScreenHandler.syncId, 157, 7, UITheme.TEXT, false);
        context.drawText(textRenderer, "Rev: " + mc.player.currentScreenHandler.getRevision(), 157, 17, UITheme.TEXT, false);
    }

    public static void createWidgets(MinecraftClient mc, Screen screen) {
        int x = 4;
        int y = 4;
        int w = 145;
        int h = 18;
        int spacing = 2;

        screen.addDrawableChild(CustomButtonWidget.create(x, y, w, Text.of("Close without packet"), (button) -> {
            mc.setScreen(null);
        }));

        screen.addDrawableChild(CustomButtonWidget.create(x, y + h + spacing, w, Text.of("Desync"), (button) -> {
            if (mc.getNetworkHandler() != null && mc.player != null) {
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            }
        }));

        screen.addDrawableChild(CustomButtonWidget.create(x, y + (h + spacing) * 2, w, Text.of("Send packets: " + SharedVariables.sendUIPackets), (button) -> {
            SharedVariables.sendUIPackets = !SharedVariables.sendUIPackets;
            button.setMessage(Text.of("Send packets: " + SharedVariables.sendUIPackets));
        }));

        screen.addDrawableChild(CustomButtonWidget.create(x, y + (h + spacing) * 3, w, Text.of("Delay packets: " + SharedVariables.delayUIPackets), (button) -> {
            SharedVariables.delayUIPackets = !SharedVariables.delayUIPackets;
            button.setMessage(Text.of("Delay packets: " + SharedVariables.delayUIPackets));
            if (!SharedVariables.delayUIPackets && !SharedVariables.delayedUIPackets.isEmpty() && mc.getNetworkHandler() != null) {
                for (Packet<?> packet : SharedVariables.delayedUIPackets) {
                    mc.getNetworkHandler().sendPacket(packet);
                }
                if (mc.player != null) {
                    mc.player.sendMessage(Text.of("§7[§c*§7] §7Sent §c" + SharedVariables.delayedUIPackets.size() + " §7packets"), false);
                }
                SharedVariables.delayedUIPackets.clear();
            }
        }));

        screen.addDrawableChild(CustomButtonWidget.create(x, y + (h + spacing) * 4, w, Text.of("Leave & Send Packets"), (button) -> {
            SharedVariables.delayUIPackets = false;
            if (mc.getNetworkHandler() != null) {
                for (Packet<?> packet : SharedVariables.delayedUIPackets) {
                    mc.getNetworkHandler().sendPacket(packet);
                }
            }
            SharedVariables.delayedUIPackets.clear();
            mc.setScreen(null);
        }));

        CustomButtonWidget fabricatePacketButton = CustomButtonWidget.create(x, y + (h + spacing) * 5, w, Text.of("Fabricate packet"), (button) -> {
            JFrame frame = new JFrame("Choose Packet");
            frame.setBounds(0, 0, 450, 100);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setLayout(null);

            JButton clickSlotButton = getPacketOptionButton("Click Slot");
            clickSlotButton.setBounds(100, 25, 110, 20);
            clickSlotButton.addActionListener((event) -> {
                frame.setVisible(false);
                showClickSlotFrame(mc);
            });

            JButton buttonClickButton = getPacketOptionButton("Button Click");
            buttonClickButton.setBounds(250, 25, 110, 20);
            buttonClickButton.addActionListener((event) -> {
                frame.setVisible(false);
                showButtonClickFrame(mc);
            });

            frame.add(clickSlotButton);
            frame.add(buttonClickButton);
            frame.setVisible(true);
        });
        fabricatePacketButton.active = !MinecraftClient.IS_SYSTEM_MAC;
        screen.addDrawableChild(fabricatePacketButton);

        screen.addDrawableChild(CustomButtonWidget.create(x, y + (h + spacing) * 6, w, Text.of("Copy GUI Title JSON"), (button) -> {
            if (mc.currentScreen != null) {
                Text title = mc.currentScreen.getTitle();
                String json = getTextAsJson(title, mc);
                mc.keyboard.setClipboard(json);
                if (mc.player != null) {
                    mc.player.sendMessage(Text.of("§7[§c*§7] §7Copied GUI title JSON to clipboard"), false);
                }
            }
        }));

        int halfW = (w - spacing) / 2;
        
        screen.addDrawableChild(CustomButtonWidget.create(x, y + (h + spacing) * 7, halfW, Text.of("Save GUI"), (button) -> {
            if (mc.player != null) {
                SharedVariables.storedScreen = mc.currentScreen;
                SharedVariables.storedScreenHandler = mc.player.currentScreenHandler;
                SharedVariables.savedScreens.put("default", mc.currentScreen);
                SharedVariables.savedScreenHandlers.put("default", mc.player.currentScreenHandler);
                if (mc.player != null) {
                    mc.player.sendMessage(Text.of("§7[§c*§7] §7GUI saved"), false);
                }
            }
        }));

        screen.addDrawableChild(CustomButtonWidget.create(x + halfW + spacing, y + (h + spacing) * 7, halfW, Text.of("Load GUI"), (button) -> {
            if (SharedVariables.storedScreen != null && SharedVariables.storedScreenHandler != null && mc.player != null) {
                mc.setScreen(SharedVariables.storedScreen);
                mc.player.currentScreenHandler = SharedVariables.storedScreenHandler;
            }
        }));

        screen.addDrawableChild(CustomButtonWidget.create(x, y + (h + spacing) * 8, halfW, Text.of("Clear Queue"), (button) -> {
            int count = SharedVariables.delayedUIPackets.size();
            SharedVariables.delayedUIPackets.clear();
            if (mc.player != null) {
                mc.player.sendMessage(Text.of("§7[§c*§7] §7Cleared §c" + count + " §7packets"), false);
            }
        }));

        screen.addDrawableChild(CustomButtonWidget.create(x + halfW + spacing, y + (h + spacing) * 8, halfW, Text.of("Queue: 0"), (button) -> {
            button.setMessage(Text.of("Queue: " + SharedVariables.delayedUIPackets.size()));
        }));

        screen.addDrawableChild(CustomButtonWidget.create(x, y + (h + spacing) * 9, halfW, Text.of("Resync Inv"), (button) -> {
            if (mc.getNetworkHandler() != null && mc.player != null) {
                mc.player.currentScreenHandler.syncState();
                mc.player.sendMessage(Text.of("§7[§c*§7] §7Inventory resynced"), false);
            }
        }));

        screen.addDrawableChild(CustomButtonWidget.create(x + halfW + spacing, y + (h + spacing) * 9, halfW, Text.of("Disconnect"), (button) -> {
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().getConnection().disconnect(Text.of("Disconnected"));
            }
        }));

        CustomButtonWidget spamButton = CustomButtonWidget.create(x + 32, y + (h + spacing) * 10, w - 64, Text.of("Spam (x" + SharedVariables.spamCount + ")"), (button) -> {
            if (mc.getNetworkHandler() != null && !SharedVariables.delayedUIPackets.isEmpty()) {
                int sent = 0;
                for (int i = 0; i < SharedVariables.spamCount; i++) {
                    for (Packet<?> packet : SharedVariables.delayedUIPackets) {
                        mc.getNetworkHandler().sendPacket(packet);
                        sent++;
                    }
                }
                if (mc.player != null) {
                    mc.player.sendMessage(Text.of("§7[§c*§7] §7Spammed §c" + sent + " §7packets"), false);
                }
            } else if (mc.player != null) {
                mc.player.sendMessage(Text.of("§7[§c*§7] §cNo packets in queue"), false);
            }
        });
        
        screen.addDrawableChild(CustomButtonWidget.create(x, y + (h + spacing) * 10, 30, Text.of("-"), (button) -> {
            if (SharedVariables.spamCount > 1) {
                SharedVariables.spamCount--;
                spamButton.setMessage(Text.of("Spam (x" + SharedVariables.spamCount + ")"));
            }
        }));

        screen.addDrawableChild(spamButton);

        screen.addDrawableChild(CustomButtonWidget.create(x + w - 30, y + (h + spacing) * 10, 30, Text.of("+"), (button) -> {
            if (SharedVariables.spamCount < 100) {
                SharedVariables.spamCount++;
                spamButton.setMessage(Text.of("Spam (x" + SharedVariables.spamCount + ")"));
            }
        }));

        screen.addDrawableChild(CustomButtonWidget.create(x, y + (h + spacing) * 11, halfW, Text.of("Send One"), (button) -> {
            if (mc.getNetworkHandler() != null && !SharedVariables.delayedUIPackets.isEmpty()) {
                Packet<?> packet = SharedVariables.delayedUIPackets.remove(0);
                mc.getNetworkHandler().sendPacket(packet);
                if (mc.player != null) {
                    mc.player.sendMessage(Text.of("§7[§c*§7] §7Sent 1 packet §7(§c" + SharedVariables.delayedUIPackets.size() + "§7 left)"), false);
                }
            } else if (mc.player != null) {
                mc.player.sendMessage(Text.of("§7[§c*§7] §cNo packets in queue"), false);
            }
        }));

        screen.addDrawableChild(CustomButtonWidget.create(x + halfW + spacing, y + (h + spacing) * 11, halfW, Text.of("Pop Last"), (button) -> {
            if (!SharedVariables.delayedUIPackets.isEmpty()) {
                SharedVariables.delayedUIPackets.remove(SharedVariables.delayedUIPackets.size() - 1);
                if (mc.player != null) {
                    mc.player.sendMessage(Text.of("§7[§c*§7] §7Removed last packet §7(§c" + SharedVariables.delayedUIPackets.size() + "§7 left)"), false);
                }
            } else if (mc.player != null) {
                mc.player.sendMessage(Text.of("§7[§c*§7] §cNo packets in queue"), false);
            }
        }));
    }

    private static String getTextAsJson(Text text, MinecraftClient mc) {
        try {
            Class<?> serializationClass = Class.forName("net.minecraft.text.Text$Serialization");
            java.lang.reflect.Method toJsonMethod = serializationClass.getMethod("toJsonString", Text.class, net.minecraft.registry.RegistryWrapper.WrapperLookup.class);
            return (String) toJsonMethod.invoke(null, text, mc.world.getRegistryManager());
        } catch (ClassNotFoundException e) {
            try {
                Class<?> serializerClass = Class.forName("net.minecraft.text.Text$Serializer");
                java.lang.reflect.Method toJsonMethod = serializerClass.getMethod("toJson", Text.class, net.minecraft.registry.RegistryWrapper.WrapperLookup.class);
                Object result = toJsonMethod.invoke(null, text, mc.world.getRegistryManager());
                return result.toString();
            } catch (Exception ex) {
                return text.getString();
            }
        } catch (Exception e) {
            return text.getString();
        }
    }

    private static void showClickSlotFrame(MinecraftClient mc) {
        JFrame clickSlotFrame = new JFrame("Click Slot Packet");
        clickSlotFrame.setBounds(0, 0, 450, 300);
        clickSlotFrame.setResizable(false);
        clickSlotFrame.setLocationRelativeTo(null);
        clickSlotFrame.setLayout(null);

        JLabel syncIdLabel = new JLabel("Sync Id:");
        syncIdLabel.setFocusable(false);
        syncIdLabel.setFont(monospace);
        syncIdLabel.setBounds(25, 25, 100, 20);

        JLabel revisionLabel = new JLabel("Revision:");
        revisionLabel.setFocusable(false);
        revisionLabel.setFont(monospace);
        revisionLabel.setBounds(25, 50, 100, 20);

        JLabel slotLabel = new JLabel("Slot:");
        slotLabel.setFocusable(false);
        slotLabel.setFont(monospace);
        slotLabel.setBounds(25, 75, 100, 20);

        JLabel buttonLabel = new JLabel("Button:");
        buttonLabel.setFocusable(false);
        buttonLabel.setFont(monospace);
        buttonLabel.setBounds(25, 100, 100, 20);

        JLabel actionLabel = new JLabel("Action:");
        actionLabel.setFocusable(false);
        actionLabel.setFont(monospace);
        actionLabel.setBounds(25, 125, 100, 20);

        JLabel timesToSendLabel = new JLabel("Times to send:");
        timesToSendLabel.setFocusable(false);
        timesToSendLabel.setFont(monospace);
        timesToSendLabel.setBounds(25, 190, 100, 20);

        JTextField syncIdField = new JTextField(1);
        syncIdField.setFont(monospace);
        syncIdField.setBounds(125, 25, 100, 20);

        JTextField revisionField = new JTextField(1);
        revisionField.setFont(monospace);
        revisionField.setBounds(125, 50, 100, 20);

        JTextField slotField = new JTextField(1);
        slotField.setFont(monospace);
        slotField.setBounds(125, 75, 100, 20);

        JTextField buttonField = new JTextField(1);
        buttonField.setFont(monospace);
        buttonField.setBounds(125, 100, 100, 20);

        JComboBox<String> actionField = new JComboBox<>(new Vector<>(ImmutableList.of(
                "PICKUP", "QUICK_MOVE", "SWAP", "CLONE", "THROW", "QUICK_CRAFT", "PICKUP_ALL"
        )));
        actionField.setFocusable(false);
        actionField.setEditable(false);
        actionField.setBorder(BorderFactory.createEmptyBorder());
        actionField.setBackground(darkWhite);
        actionField.setFont(monospace);
        actionField.setBounds(125, 125, 100, 20);

        JLabel statusLabel = new JLabel();
        statusLabel.setVisible(false);
        statusLabel.setFocusable(false);
        statusLabel.setFont(monospace);
        statusLabel.setBounds(210, 150, 190, 20);

        JCheckBox delayBox = new JCheckBox("Delay");
        delayBox.setBounds(115, 150, 85, 20);
        delayBox.setSelected(false);
        delayBox.setFont(monospace);
        delayBox.setFocusable(false);

        JTextField timesToSendField = new JTextField("1");
        timesToSendField.setFont(monospace);
        timesToSendField.setBounds(125, 190, 100, 20);

        JButton sendButton = new JButton("Send");
        sendButton.setFocusable(false);
        sendButton.setBounds(25, 150, 75, 20);
        sendButton.setBorder(BorderFactory.createEtchedBorder());
        sendButton.setBackground(darkWhite);
        sendButton.setFont(monospace);
        sendButton.addActionListener((event0) -> {
            if (isInteger(syncIdField.getText()) && isInteger(revisionField.getText()) && isInteger(slotField.getText()) && isInteger(buttonField.getText()) && isInteger(timesToSendField.getText()) && actionField.getSelectedItem() != null) {
                int syncId = Integer.parseInt(syncIdField.getText());
                int revision = Integer.parseInt(revisionField.getText());
                short slot = Short.parseShort(slotField.getText());
                byte button0 = Byte.parseByte(buttonField.getText());
                SlotActionType action = stringToSlotActionType(actionField.getSelectedItem().toString());
                int timesToSend = Integer.parseInt(timesToSendField.getText());

                if (action != null) {
                    ClickSlotC2SPacket packet = createClickSlotPacket(syncId, revision, slot, button0, action, ItemStack.EMPTY, new Int2ObjectArrayMap<>());
                    try {
                        Runnable toRun = getFabricatePacketRunnable(mc, delayBox.isSelected(), packet);
                        for (int i = 0; i < timesToSend; i++) {
                            toRun.run();
                        }
                    } catch (Exception e) {
                        statusLabel.setForeground(Color.RED.darker());
                        statusLabel.setText("Must be connected to a server!");
                        statusLabel.setVisible(true);
                        queueTask(() -> { statusLabel.setVisible(false); statusLabel.setText(""); }, 1500L);
                        return;
                    }
                    statusLabel.setVisible(true);
                    statusLabel.setForeground(Color.GREEN.darker());
                    statusLabel.setText("Sent!");
                    queueTask(() -> { statusLabel.setVisible(false); statusLabel.setText(""); }, 1500L);
                } else {
                    statusLabel.setVisible(true);
                    statusLabel.setForeground(Color.RED.darker());
                    statusLabel.setText("Invalid!");
                    queueTask(() -> { statusLabel.setVisible(false); statusLabel.setText(""); }, 1500L);
                }
            } else {
                statusLabel.setVisible(true);
                statusLabel.setForeground(Color.RED.darker());
                statusLabel.setText("Invalid!");
                queueTask(() -> { statusLabel.setVisible(false); statusLabel.setText(""); }, 1500L);
            }
        });

        clickSlotFrame.add(syncIdLabel);
        clickSlotFrame.add(revisionLabel);
        clickSlotFrame.add(slotLabel);
        clickSlotFrame.add(buttonLabel);
        clickSlotFrame.add(actionLabel);
        clickSlotFrame.add(timesToSendLabel);
        clickSlotFrame.add(syncIdField);
        clickSlotFrame.add(revisionField);
        clickSlotFrame.add(slotField);
        clickSlotFrame.add(buttonField);
        clickSlotFrame.add(actionField);
        clickSlotFrame.add(sendButton);
        clickSlotFrame.add(statusLabel);
        clickSlotFrame.add(delayBox);
        clickSlotFrame.add(timesToSendField);
        clickSlotFrame.setVisible(true);
    }

    private static void showButtonClickFrame(MinecraftClient mc) {
        JFrame buttonClickFrame = new JFrame("Button Click Packet");
        buttonClickFrame.setBounds(0, 0, 450, 250);
        buttonClickFrame.setResizable(false);
        buttonClickFrame.setLocationRelativeTo(null);
        buttonClickFrame.setLayout(null);

        JLabel syncIdLabel = new JLabel("Sync Id:");
        syncIdLabel.setFocusable(false);
        syncIdLabel.setFont(monospace);
        syncIdLabel.setBounds(25, 25, 100, 20);

        JLabel buttonIdLabel = new JLabel("Button Id:");
        buttonIdLabel.setFocusable(false);
        buttonIdLabel.setFont(monospace);
        buttonIdLabel.setBounds(25, 50, 100, 20);

        JTextField syncIdField = new JTextField(1);
        syncIdField.setFont(monospace);
        syncIdField.setBounds(125, 25, 100, 20);

        JTextField buttonIdField = new JTextField(1);
        buttonIdField.setFont(monospace);
        buttonIdField.setBounds(125, 50, 100, 20);

        JLabel statusLabel = new JLabel();
        statusLabel.setVisible(false);
        statusLabel.setFocusable(false);
        statusLabel.setFont(monospace);
        statusLabel.setBounds(210, 95, 190, 20);

        JCheckBox delayBox = new JCheckBox("Delay");
        delayBox.setBounds(115, 95, 85, 20);
        delayBox.setSelected(false);
        delayBox.setFont(monospace);
        delayBox.setFocusable(false);

        JLabel timesToSendLabel = new JLabel("Times to send:");
        timesToSendLabel.setFocusable(false);
        timesToSendLabel.setFont(monospace);
        timesToSendLabel.setBounds(25, 130, 100, 20);

        JTextField timesToSendField = new JTextField("1");
        timesToSendField.setFont(monospace);
        timesToSendField.setBounds(125, 130, 100, 20);

        JButton sendButton = new JButton("Send");
        sendButton.setFocusable(false);
        sendButton.setBounds(25, 95, 75, 20);
        sendButton.setBorder(BorderFactory.createEtchedBorder());
        sendButton.setBackground(darkWhite);
        sendButton.setFont(monospace);
        sendButton.addActionListener((event0) -> {
            if (isInteger(syncIdField.getText()) && isInteger(buttonIdField.getText()) && isInteger(timesToSendField.getText())) {
                int syncId = Integer.parseInt(syncIdField.getText());
                int buttonId = Integer.parseInt(buttonIdField.getText());
                int timesToSend = Integer.parseInt(timesToSendField.getText());

                ButtonClickC2SPacket packet = new ButtonClickC2SPacket(syncId, buttonId);
                try {
                    Runnable toRun = getFabricatePacketRunnable(mc, delayBox.isSelected(), packet);
                    for (int i = 0; i < timesToSend; i++) {
                        toRun.run();
                    }
                } catch (Exception e) {
                    statusLabel.setVisible(true);
                    statusLabel.setForeground(Color.RED.darker());
                    statusLabel.setText("Must be connected!");
                    queueTask(() -> { statusLabel.setVisible(false); statusLabel.setText(""); }, 1500L);
                    return;
                }
                statusLabel.setVisible(true);
                statusLabel.setForeground(Color.GREEN.darker());
                statusLabel.setText("Sent!");
                queueTask(() -> { statusLabel.setVisible(false); statusLabel.setText(""); }, 1500L);
            } else {
                statusLabel.setVisible(true);
                statusLabel.setForeground(Color.RED.darker());
                statusLabel.setText("Invalid!");
                queueTask(() -> { statusLabel.setVisible(false); statusLabel.setText(""); }, 1500L);
            }
        });

        buttonClickFrame.add(syncIdLabel);
        buttonClickFrame.add(buttonIdLabel);
        buttonClickFrame.add(syncIdField);
        buttonClickFrame.add(timesToSendLabel);
        buttonClickFrame.add(buttonIdField);
        buttonClickFrame.add(sendButton);
        buttonClickFrame.add(statusLabel);
        buttonClickFrame.add(delayBox);
        buttonClickFrame.add(timesToSendField);
        buttonClickFrame.setVisible(true);
    }
    @NotNull
    private static JButton getPacketOptionButton(String label) {
        JButton button = new JButton(label);
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEtchedBorder());
        button.setBackground(darkWhite);
        button.setFont(monospace);
        return button;
    }

    @NotNull
    private static Runnable getFabricatePacketRunnable(MinecraftClient mc, boolean delay, Packet<?> packet) {
        Runnable toRun;
        if (delay) {
            toRun = () -> {
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendPacket(packet);
                }
            };
        } else {
            toRun = () -> {
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendPacket(packet);
                }
                ((ClientConnectionAccessor) mc.getNetworkHandler().getConnection()).getChannel().writeAndFlush(packet);
            };
        }
        return toRun;
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
        return switch (string) {
            case "PICKUP" -> SlotActionType.PICKUP;
            case "QUICK_MOVE" -> SlotActionType.QUICK_MOVE;
            case "SWAP" -> SlotActionType.SWAP;
            case "CLONE" -> SlotActionType.CLONE;
            case "THROW" -> SlotActionType.THROW;
            case "QUICK_CRAFT" -> SlotActionType.QUICK_CRAFT;
            case "PICKUP_ALL" -> SlotActionType.PICKUP_ALL;
            default -> null;
        };
    }

    public static void queueTask(Runnable runnable, long delayMs) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                MinecraftClient.getInstance().send(runnable);
            }
        };
        timer.schedule(task, delayMs);
    }

    public static String getModVersion(String modId) {
        ModMetadata modMetadata = FabricLoader.getInstance().getModContainer(modId).isPresent() ? FabricLoader.getInstance().getModContainer(modId).get().getMetadata() : null;
        return modMetadata != null ? modMetadata.getVersion().getFriendlyString() : "null";
    }

    @SuppressWarnings("unchecked")
    public static ClickSlotC2SPacket createClickSlotPacket(int syncId, int revision, int slot, int button, SlotActionType action, ItemStack stack, Int2ObjectMap<ItemStack> changedSlots) {
        try {
            for (Constructor<?> constructor : ClickSlotC2SPacket.class.getDeclaredConstructors()) {
                Class<?>[] params = constructor.getParameterTypes();
                if (params.length == 7) {
                    constructor.setAccessible(true);
                    if (Int2ObjectMap.class.isAssignableFrom(params[5])) {
                        return (ClickSlotC2SPacket) constructor.newInstance(syncId, revision, slot, button, action, changedSlots, stack);
                    } else if (ItemStack.class.isAssignableFrom(params[5])) {
                        return (ClickSlotC2SPacket) constructor.newInstance(syncId, revision, slot, button, action, stack, changedSlots);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to create ClickSlotC2SPacket via reflection", e);
        }
        return null;
    }
}
