package org.uiutils.mixin;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.main.Main;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiutils.MainClient;
import org.uiutils.SharedVariables;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@Mixin(HandledScreen.class)
public class HandledScreenMixin extends Screen {
    protected HandledScreenMixin(Text title) {
        super(title);
    }

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // called when creating a HandledScreen
    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {

        // register "close without packet" button in all HandledScreens
        addDrawableChild(ButtonWidget.builder(Text.of("Close without packet"), (button) -> {

            // closes the current gui without sending a packet to the current server
            mc.setScreen(null);
        }).width(160).position(5, 5).build());

        // register "de-sync" button in all HandledScreens
        addDrawableChild(ButtonWidget.builder(Text.of("De-sync"), (button) -> {

            // keeps the current gui open client-side and closed server-side
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        }).width(90).position(5, 35).build());

        // register "send packets" button in all HandledScreens
        addDrawableChild(ButtonWidget.builder(Text.of("Send packets: " + SharedVariables.sendUIPackets), (button) -> {

            // tells the client if it should send any gui related packets
            SharedVariables.sendUIPackets = !SharedVariables.sendUIPackets;
            button.setMessage(Text.of("Send packets: " + SharedVariables.sendUIPackets));
        }).width(160).position(5, 65).build());

        // register "delay packets" button in all HandledScreens
        addDrawableChild(ButtonWidget.builder(Text.of("Delay packets: " + SharedVariables.delayUIPackets), (button) -> {

            // toggles a setting to delay all gui related packets to be used later when turning this setting off
            SharedVariables.delayUIPackets = !SharedVariables.delayUIPackets;
            button.setMessage(Text.of("Delay packets: " + SharedVariables.delayUIPackets));
            if (!SharedVariables.delayUIPackets && !SharedVariables.delayedUIPackets.isEmpty()) {
                for (Packet<?> packet : SharedVariables.delayedUIPackets) {
                    mc.getNetworkHandler().sendPacket(packet);
                }
                SharedVariables.delayedUIPackets.clear();
            }
        }).width(160).position(5, 95).build());

        // register "save gui" button in all HandledScreens
        addDrawableChild(ButtonWidget.builder(Text.of("Save GUI"), (button) -> {

            // saves the current gui to a variable to be accessed later
            SharedVariables.storedScreen = mc.currentScreen;
            SharedVariables.storedScreenHandler = mc.player.currentScreenHandler;
        }).width(160).position(5, 125).build());

        // register "disconnect and send packets" button in all HandledScreens
        addDrawableChild(ButtonWidget.builder(Text.of("Disconnect and send packets"), (button) -> {

            // sends all "delayed" gui related packets before disconnecting, use: potential race conditions on non-vanilla servers
            if (!SharedVariables.delayedUIPackets.isEmpty()) {
                SharedVariables.delayUIPackets = false;
                for (Packet<?> packet : SharedVariables.delayedUIPackets) {
                    mc.getNetworkHandler().sendPacket(packet);
                }
                mc.getNetworkHandler().getConnection().disconnect(Text.of("Disconnecting (UI UTILS)"));
                SharedVariables.delayedUIPackets.clear();
            }
        }).width(200).position(5, 155).build());

        // register "fabricate packet" button in all HandledScreens
        addDrawableChild(ButtonWidget.builder(Text.of("Fabricate packet"), (button) -> {

            // creates a gui allowing you to fabricate packets

            JFrame frame = new JFrame("Choose Packet");

            JButton clickSlotButton = new JButton("Click Slot");
            clickSlotButton.setBounds(100, 25, 110, 20);
            clickSlotButton.setFocusable(false);
            clickSlotButton.addActionListener((event) -> {
                // im too lazy to comment everything here just read the code yourself

                frame.setVisible(false);

                JFrame clickSlotFrame = new JFrame("Click Slot Packet");

                JLabel syncIdLabel = new JLabel("Sync Id:");
                syncIdLabel.setFocusable(false);
                syncIdLabel.setBounds(25, 25, 100, 20);

                JLabel revisionLabel = new JLabel("Revision:");
                revisionLabel.setFocusable(false);
                revisionLabel.setBounds(25, 50, 100, 20);

                JLabel slotLabel = new JLabel("Slot:");
                slotLabel.setFocusable(false);
                slotLabel.setBounds(25, 75, 100, 20);

                JLabel buttonLabel = new JLabel("Button:");
                buttonLabel.setFocusable(false);
                buttonLabel.setBounds(25, 100, 100, 20);

                JLabel actionLabel = new JLabel("Action:");
                actionLabel.setFocusable(false);
                actionLabel.setBounds(25, 125, 100, 20);

                JTextField syncIdField = new JTextField(1);
                syncIdField.setBounds(125, 25, 100, 20);

                JTextField revisionField = new JTextField(1);
                revisionField.setBounds(125, 50, 100, 20);

                JTextField slotField = new JTextField(1);
                slotField.setBounds(125, 75, 100, 20);

                JTextField buttonField = new JTextField(1);
                buttonField.setBounds(125, 100, 100, 20);

                JComboBox<String> actionField = new JComboBox<>();
                List<String> actions = ImmutableList.of(
                        "PICKUP",
                        "QUICK_MOVE",
                        "SWAP",
                        "CLONE",
                        "THROW",
                        "QUICK_CRAFT",
                        "PICKUP_ALL"
                );
                actionField.setEditable(false);
                actionField.setFocusable(false);
                actionField.setBounds(125, 125, 100, 20);
                for (String action : actions) {
                    actionField.addItem(action);
                }

                JLabel statusLabel = new JLabel();
                statusLabel.setForeground(Color.WHITE);
                statusLabel.setFocusable(false);
                statusLabel.setBounds(125, 150, 125, 20);

                JButton sendButton = new JButton("Send");
                sendButton.setFocusable(false);
                sendButton.setBounds(25, 150, 75, 20);
                sendButton.addActionListener((event0) -> {
                    if (
                            syncIdField.getText().isEmpty() ||
                                    revisionField.getText().isEmpty() ||
                                    slotField.getText().isEmpty() ||
                                    buttonField.getText().isEmpty())
                    {
                        statusLabel.setForeground(Color.RED.darker());
                        statusLabel.setText("Invalid parameters!");
                        MainClient.queueTask(() -> {
                            statusLabel.setForeground(Color.WHITE);
                            statusLabel.setText("");
                        }, 1500L);
                        return;
                    }
                    if (
                            MainClient.isInteger(syncIdField.getText()) &&
                                    MainClient.isInteger(revisionField.getText()) &&
                                    MainClient.isInteger(slotField.getText()) &&
                                    MainClient.isInteger(buttonField.getText()) &&
                                    actionField.getSelectedItem() != null)
                    {
                        int syncId = Integer.parseInt(syncIdField.getText());
                        int revision = Integer.parseInt(revisionField.getText());
                        int slot = Integer.parseInt(slotField.getText());
                        int button0 = Integer.parseInt(buttonField.getText());
                        SlotActionType action = MainClient.stringToSlotActionType(actionField.getSelectedItem().toString());

                        if (action != null) {
                            mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(syncId, revision, slot, button0, action, ItemStack.EMPTY, new Int2ObjectArrayMap<>()));
                            statusLabel.setForeground(Color.GREEN.darker());
                            statusLabel.setText("Sent successfully!");
                            MainClient.queueTask(() -> {
                                statusLabel.setForeground(Color.WHITE);
                                statusLabel.setText("");
                            }, 1500L);
                        } else {
                            statusLabel.setForeground(Color.RED.darker());
                            statusLabel.setText("Invalid parameters!");
                            MainClient.queueTask(() -> {
                                statusLabel.setForeground(Color.WHITE);
                                statusLabel.setText("");
                            }, 1500L);
                        }
                    } else {
                        statusLabel.setForeground(Color.RED.darker());
                        statusLabel.setText("Invalid parameters!");
                        MainClient.queueTask(() -> {
                            statusLabel.setForeground(Color.WHITE);
                            statusLabel.setText("");
                        }, 1500L);
                    }
                });

                clickSlotFrame.setBounds(0, 0, 450, 250);
                clickSlotFrame.setLayout(null);
                clickSlotFrame.setLocationRelativeTo(null);
                clickSlotFrame.add(syncIdLabel);
                clickSlotFrame.add(revisionLabel);
                clickSlotFrame.add(slotLabel);
                clickSlotFrame.add(buttonLabel);
                clickSlotFrame.add(actionLabel);
                clickSlotFrame.add(syncIdField);
                clickSlotFrame.add(revisionField);
                clickSlotFrame.add(slotField);
                clickSlotFrame.add(buttonField);
                clickSlotFrame.add(actionField);
                clickSlotFrame.add(sendButton);
                clickSlotFrame.add(statusLabel);
                clickSlotFrame.setVisible(true);
            });

            JButton buttonClickButton = new JButton("Button Click");
            buttonClickButton.setBounds(220, 25, 110, 20);
            buttonClickButton.setFocusable(false);
            buttonClickButton.addActionListener((event) -> {
                // im too lazy to comment everything here just read the code yourself

                frame.setVisible(false);

                JFrame buttonClickFrame = new JFrame("Button Click Packet");

                JLabel syncIdLabel = new JLabel("Sync Id:");
                syncIdLabel.setFocusable(false);
                syncIdLabel.setBounds(25, 25, 100, 20);

                JLabel buttonIdLabel = new JLabel("Button Id:");
                buttonIdLabel.setFocusable(false);
                buttonIdLabel.setBounds(25, 50, 100, 20);

                JTextField syncIdField = new JTextField(1);
                syncIdField.setBounds(125, 25, 100, 20);

                JTextField buttonIdField = new JTextField(1);
                buttonIdField.setBounds(125, 50, 100, 20);

                JLabel statusLabel = new JLabel();
                statusLabel.setForeground(Color.WHITE);
                statusLabel.setFocusable(false);
                statusLabel.setBounds(125, 150, 125, 20);

                JButton sendButton = new JButton("Send");
                sendButton.setFocusable(false);
                sendButton.setBounds(25, 150, 75, 20);
                sendButton.addActionListener((event0) -> {
                    if (syncIdField.getText().isEmpty() || buttonIdField.getText().isEmpty()) {
                        statusLabel.setForeground(Color.RED.darker());
                        statusLabel.setText("Invalid parameters!");
                        MainClient.queueTask(() -> {
                            statusLabel.setForeground(Color.WHITE);
                            statusLabel.setText("");
                        }, 1500L);
                        return;
                    }
                    if (MainClient.isInteger(syncIdField.getText()) && MainClient.isInteger(buttonIdField.getText())) {
                        int syncId = Integer.parseInt(syncIdField.getText());
                        int buttonId = Integer.parseInt(buttonIdField.getText());

                        mc.getNetworkHandler().sendPacket(new ButtonClickC2SPacket(syncId, buttonId));
                        statusLabel.setForeground(Color.GREEN.darker());
                        statusLabel.setText("Sent successfully!");
                        MainClient.queueTask(() -> {
                            statusLabel.setForeground(Color.WHITE);
                            statusLabel.setText("");
                        }, 1500L);
                    } else {
                        statusLabel.setForeground(Color.RED.darker());
                        statusLabel.setText("Invalid parameters!");
                        MainClient.queueTask(() -> {
                            statusLabel.setForeground(Color.WHITE);
                            statusLabel.setText("");
                        }, 1500L);
                    }
                });

                buttonClickFrame.setBounds(0, 0, 450, 250);
                buttonClickFrame.setLayout(null);
                buttonClickFrame.setLocationRelativeTo(null);
                buttonClickFrame.add(syncIdLabel);
                buttonClickFrame.add(buttonIdLabel);
                buttonClickFrame.add(syncIdField);
                buttonClickFrame.add(buttonIdField);
                buttonClickFrame.add(sendButton);
                buttonClickFrame.add(statusLabel);
                buttonClickFrame.setVisible(true);
            });

            frame.setBounds(0, 0, 450, 100);
            frame.setLayout(null);
            frame.setLocationRelativeTo(null);
            frame.add(clickSlotButton);
            frame.add(buttonClickButton);
            frame.setVisible(true);
        }).width(200).position(5, 185).build());
    }

    // inject at the end of the render method
    @Inject(at = @At("TAIL"), method = "render")
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {

        // display sync id and revision
        MainClient.renderHandledScreen(mc, textRenderer, matrices);
    }
}
