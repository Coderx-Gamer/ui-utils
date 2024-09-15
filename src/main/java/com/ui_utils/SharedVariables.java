package com.ui_utils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.ScreenHandler;

import java.util.ArrayList;

public class SharedVariables {
    public static boolean sendUIPackets = true;
    public static boolean delayUIPackets = false;
    public static boolean shouldEditSign = true;

    public static ArrayList<Packet<?>> delayedUIPackets = new ArrayList<>();

    public static Screen storedScreen = null;
    public static ScreenHandler storedScreenHandler = null;

    public static boolean enabled = true;
    public static boolean bypassResourcePack = false;
    public static boolean resourcePackForceDeny = false;
}
