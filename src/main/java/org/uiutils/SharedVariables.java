package org.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;

public class SharedVariables {
    public static boolean sendUIPackets = true;
    public static boolean delayUIPackets = false;
    public static boolean shouldEditSign = true;

    public static ArrayList<Packet<?>> delayedUIPackets = new ArrayList<>();

    public static Screen storedScreen = null;
    public static ScreenHandler storedScreenHandler = null;

    public static boolean enabled = true;

    public static int syncId = 0;
    public static int revision = 0;
    public static int slot = 0;
    public static int button0 = 0;
    public static SlotActionType action = null;
}
