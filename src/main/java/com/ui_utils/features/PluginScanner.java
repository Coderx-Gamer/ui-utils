package com.ui_utils.features;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.text.Text;

import java.util.*;

public class PluginScanner {
    private static final String P = "§7[§c*§7] ";
    
    private static final Set<String> ANTICHEAT_LIST = Set.of(
        "nocheatplus", "negativity", "warden", "horizon", "illegalstack", 
        "coreprotect", "exploitsx", "vulcan", "abc", "spartan", "kauri", 
        "anticheatreloaded", "witherac", "godseye", "matrix", "wraith", 
        "antixrayheuristics", "grimac", "themis", "foxaddition", "guardianac", 
        "ggintegrity", "lightanticheat", "anarchyexploitfixes"
    );
    
    private static final Random RANDOM = new Random();
    private static boolean scanning = false;
    private static int ticksWaiting = 0;
    private static final List<String> foundPlugins = new ArrayList<>();
    
    public static void startScan() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null || mc.player == null) {
            return;
        }
        
        if (scanning) return;
        
        foundPlugins.clear();
        scanning = true;
        ticksWaiting = 0;
        
        mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(RANDOM.nextInt(200), "ver "));
    }
    
    public static void onTick() {
        if (!scanning) return;
        
        ticksWaiting++;
        if (ticksWaiting >= 60) {
            printResults();
        }
    }
    
    public static void onCommandSuggestions(CommandSuggestionsS2CPacket packet) {
        if (!scanning) return;
        
        scanning = false;
        
        try {
            Suggestions suggestions = packet.getSuggestions();
            MinecraftClient mc = MinecraftClient.getInstance();
            
            if (suggestions.isEmpty()) {
                if (mc.player != null) {
                    mc.player.sendMessage(Text.of(P + "§cNo plugins found or blocked"), false);
                }
                return;
            }
            
            for (Suggestion suggestion : suggestions.getList()) {
                String pluginName = suggestion.getText().trim();
                if (!pluginName.isEmpty() && !foundPlugins.contains(pluginName.toLowerCase())) {
                    foundPlugins.add(pluginName);
                }
            }
            
            printResults();
        } catch (Exception e) {
            scanning = false;
        }
    }
    
    private static void printResults() {
        scanning = false;
        ticksWaiting = 0;
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        if (foundPlugins.isEmpty()) {
            mc.player.sendMessage(Text.of(P + "§cNo plugins found"), false);
            return;
        }
        
        foundPlugins.sort(String.CASE_INSENSITIVE_ORDER);
        
        StringBuilder sb = new StringBuilder();
        sb.append(P).append("Plugins §7(§c").append(foundPlugins.size()).append("§7): ");
        
        for (int i = 0; i < foundPlugins.size(); i++) {
            String plugin = foundPlugins.get(i);
            if (ANTICHEAT_LIST.contains(plugin.toLowerCase()) || 
                plugin.toLowerCase().contains("exploit") || 
                plugin.toLowerCase().contains("cheat") || 
                plugin.toLowerCase().contains("illegal")) {
                sb.append("§4");
            } else {
                sb.append("§c");
            }
            sb.append(plugin);
            if (i < foundPlugins.size() - 1) {
                sb.append("§7, ");
            }
        }
        
        mc.player.sendMessage(Text.of(sb.toString()), false);
        foundPlugins.clear();
    }
    
    public static boolean isScanning() {
        return scanning;
    }
}
