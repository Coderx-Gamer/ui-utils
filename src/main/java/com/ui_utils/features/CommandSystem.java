package com.ui_utils.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.session.Session;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.text.Text;
import com.ui_utils.SharedVariables;

import java.util.*;

public class CommandSystem {
    private static final String P = "§7[§c*§7] ";
    
    private static final Map<String, CommandHandler> commands = new LinkedHashMap<>();
    private static final Map<String, String> manuals = new LinkedHashMap<>();

    static {
        registerCommand("help", CommandSystem::helpCommand, "§7Lists all commands or shows help for one\n§cUsage: §7,help [command]");
        registerCommand("man", CommandSystem::manCommand, "§7Shows detailed manual for a command\n§cUsage: §7,man <command>");
        registerCommand("toggleuiutils", CommandSystem::toggleCommand, "§7Toggles UI-Utils overlay on/off\n§cUsage: §7,toggleuiutils");
        registerCommand("echo", CommandSystem::echoCommand, "§7Prints text to chat (client-side)\n§cUsage: §7,echo <text>");
        registerCommand("math", CommandSystem::mathCommand, "§7Calculates math expression\n§cSupports: §7+ - * / ^ ()\n§cUsage: §7,math <expression>\n§cExample: §7,math 2+2*3");
        registerCommand("close", CommandSystem::closeCommand, "§7Closes current screen without packet\n§cUsage: §7,close");
        registerCommand("desync", CommandSystem::desyncCommand, "§7Sends close packet but keeps GUI open\n§cUsage: §7,desync");
        registerCommand("chat", CommandSystem::chatCommand, "§7Sends message/command to server\n§cUsage: §7,chat <message>\n§cExample: §7,chat /spawn");
        registerCommand("joinserver", CommandSystem::joinServerCommand, "§7Connects to another server\n§cUsage: §7,joinserver <ip[:port]>\n§cExample: §7,joinserver mc.hypixel.net");
        registerCommand("screen", CommandSystem::screenCommand, "§7Manages saved screens\n§cActions:\n§7  save <slot> - Save current screen\n§7  load <slot> - Load saved screen\n§7  list - List all saved slots\n§7  info <slot> - Show screen info");
        registerCommand("account", CommandSystem::accountCommand, "§7View/change session info\n§cActions:\n§7  dump - Show current username/UUID\n§7  set username <name> - Change username\n§7  set uuid <uuid> - Change UUID\n§cNote: §7Changes are temporary until restart");
        registerCommand("prefix", CommandSystem::prefixCommand, "§7View/change command prefix\n§cUsage: §7,prefix [new]\n§cExample: §7,prefix .");
        registerCommand("vclip", CommandSystem::vclipCommand, "§7Teleports vertically (client-side)\n§cUsage: §7,vclip <blocks>\n§cExample: §7,vclip 5 (up), ,vclip -3 (down)");
        registerCommand("hclip", CommandSystem::hclipCommand, "§7Teleports horizontally in look direction\n§cUsage: §7,hclip <blocks>\n§cExample: §7,hclip 10");
        registerCommand("plugins", CommandSystem::pluginsCommand, "§7Scans server for plugins via tab-complete\n§cUsage: §7,plugins\n§4Red §7= Anticheat/exploit related");
    }

    public static void registerCommand(String name, CommandHandler handler, String manual) {
        commands.put(name.toLowerCase(), handler);
        manuals.put(name.toLowerCase(), manual);
    }

    public static String execute(String input) {
        if (input == null || input.trim().isEmpty()) {
            return P + "§cNo command";
        }

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        CommandHandler handler = commands.get(commandName);
        if (handler == null) {
            return P + "§cUnknown: §7" + commandName;
        }

        try {
            return handler.execute(args);
        } catch (Exception e) {
            return P + "§cError: §7" + e.getMessage();
        }
    }

    private static String helpCommand(String args) {
        if (!args.isEmpty()) {
            return manCommand(args);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(P).append("Commands: ");
        int i = 0;
        for (String cmd : commands.keySet()) {
            sb.append("§c").append(cmd);
            if (i < commands.size() - 1) {
                sb.append("§7, ");
            }
            i++;
        }
        return sb.toString();
    }

    private static String manCommand(String args) {
        if (args.isEmpty()) {
            return P + "§cUsage: man <command>";
        }
        String manual = manuals.get(args.toLowerCase());
        if (manual == null) {
            return P + "§cNo manual for: §7" + args;
        }
        return P + manual.replace("\n", "\n" + P);
    }

    private static String toggleCommand(String args) {
        SharedVariables.enabled = !SharedVariables.enabled;
        return "§7UI-Utils: " + (SharedVariables.enabled ? "§aON" : "§cOFF");
    }

    private static String echoCommand(String args) {
        return args.isEmpty() ? "" : P + args;
    }

    private static String mathCommand(String args) {
        if (args.isEmpty()) {
            return P + "§cUsage: math <expression>";
        }
        try {
            double result = evaluateExpression(args);
            return P + "Result: §c" + result;
        } catch (Exception e) {
            return P + "§cInvalid expression";
        }
    }

    private static String closeCommand(String args) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.execute(() -> mc.setScreen(null));
        return P + "Screen closed";
    }

    private static String desyncCommand(String args) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null || mc.player == null) {
            return P + "§cNot connected";
        }
        int syncId = mc.player.currentScreenHandler.syncId;
        mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(syncId));
        return P + "Desync sent §7(syncId: §c" + syncId + "§7)";
    }

    private static String chatCommand(String args) {
        if (args.isEmpty()) {
            return P + "§cUsage: chat <message>";
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return P + "§cNot in game";
        }
        mc.execute(() -> {
            if (args.startsWith("/")) {
                mc.player.networkHandler.sendChatCommand(args.substring(1));
            } else {
                mc.player.networkHandler.sendChatMessage(args);
            }
        });
        return P + "Sent: §c" + args;
    }

    private static String joinServerCommand(String args) {
        if (args.isEmpty()) {
            return P + "§cUsage: joinserver <ip>";
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        String serverIp = args.split("\\s+")[0];
        ServerAddress address = ServerAddress.parse(serverIp);
        ServerInfo serverInfo = new ServerInfo("Server", serverIp, ServerInfo.ServerType.OTHER);
        
        mc.execute(() -> {
            try {
                if (mc.world != null) {
                    try {
                        mc.world.getClass().getMethod("disconnect").invoke(mc.world);
                    } catch (NoSuchMethodException e) {
                        mc.world.getClass().getMethod("disconnect", Text.class).invoke(mc.world, Text.empty());
                    }
                }
                try {
                    mc.getClass().getMethod("disconnect").invoke(mc);
                } catch (NoSuchMethodException e) {
                    mc.getClass().getMethod("disconnect", net.minecraft.client.gui.screen.Screen.class, boolean.class)
                        .invoke(mc, new TitleScreen(), false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), mc, address, serverInfo, false, null);
        });
        return P + "Joining: §c" + serverIp;
    }

    private static String screenCommand(String args) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length == 0 || parts[0].isEmpty()) {
            return P + "§cUsage: screen <save|load|list|info> [slot]";
        }
        String action = parts[0].toLowerCase();
        String slot = parts.length > 1 ? parts[1] : "";
        MinecraftClient mc = MinecraftClient.getInstance();

        return switch (action) {
            case "save" -> {
                if (slot.isEmpty()) yield P + "§cUsage: screen save <slot>";
                if (mc.currentScreen == null || mc.player == null) yield P + "§cNo screen to save";
                SharedVariables.savedScreens.put(slot, mc.currentScreen);
                SharedVariables.savedScreenHandlers.put(slot, mc.player.currentScreenHandler);
                yield P + "Saved to: §c" + slot;
            }
            case "load" -> {
                if (slot.isEmpty()) yield P + "§cUsage: screen load <slot>";
                Screen screen = SharedVariables.savedScreens.get(slot);
                if (screen == null) yield P + "§cNo screen in slot: §7" + slot;
                mc.execute(() -> {
                    mc.setScreen(screen);
                    if (mc.player != null && SharedVariables.savedScreenHandlers.containsKey(slot)) {
                        mc.player.currentScreenHandler = SharedVariables.savedScreenHandlers.get(slot);
                    }
                });
                yield P + "Loaded: §c" + slot;
            }
            case "list" -> {
                if (SharedVariables.savedScreens.isEmpty()) yield P + "No saved screens";
                yield P + "Slots: §c" + String.join("§7, §c", SharedVariables.savedScreens.keySet());
            }
            case "info" -> {
                if (slot.isEmpty()) yield P + "§cUsage: screen info <slot>";
                Screen screen = SharedVariables.savedScreens.get(slot);
                if (screen == null) yield P + "§cNo screen in slot: §7" + slot;
                yield P + "Slot: §c" + slot + " §7| Screen: §c" + screen.getClass().getSimpleName();
            }
            default -> P + "§cUnknown action";
        };
    }

    private static String accountCommand(String args) {
        String[] parts = args.split("\\s+", 3);
        if (parts.length == 0 || parts[0].isEmpty()) {
            return P + "§cUsage: account <dump|set> [args]";
        }
        String action = parts[0].toLowerCase();
        MinecraftClient mc = MinecraftClient.getInstance();

        return switch (action) {
            case "dump" -> {
                Session session = mc.getSession();
                yield P + "Username: §c" + session.getUsername() + "\n" + P + "UUID: §c" + session.getUuidOrNull();
            }
            case "set" -> {
                if (parts.length < 3) yield P + "§cUsage: account set <username|uuid> <value>";
                String type = parts[1].toLowerCase();
                String value = parts[2];
                
                try {
                    Session oldSession = mc.getSession();
                    Session newSession;
                    
                    if (type.equals("username")) {
                        newSession = new Session(value, oldSession.getUuidOrNull(), oldSession.getAccessToken(), java.util.Optional.empty(), java.util.Optional.empty(), Session.AccountType.MOJANG);
                    } else if (type.equals("uuid")) {
                        java.util.UUID uuid = java.util.UUID.fromString(value);
                        newSession = new Session(oldSession.getUsername(), uuid, oldSession.getAccessToken(), java.util.Optional.empty(), java.util.Optional.empty(), Session.AccountType.MOJANG);
                    } else {
                        yield P + "§cUnknown type: §7" + type + " §c(use username or uuid)";
                    }
                    
                    java.lang.reflect.Field sessionField = MinecraftClient.class.getDeclaredField("session");
                    sessionField.setAccessible(true);
                    sessionField.set(mc, newSession);
                    
                    yield P + "Set §c" + type + " §7= §c" + value;
                } catch (IllegalArgumentException e) {
                    yield P + "§cInvalid UUID format";
                } catch (Exception e) {
                    yield P + "§cFailed to set " + type + ": §7" + e.getMessage();
                }
            }
            default -> P + "§cUnknown action: §7" + action;
        };
    }

    private static String prefixCommand(String args) {
        if (args.isEmpty()) {
            return P + "Current prefix: §c" + SharedVariables.commandPrefix;
        }
        String newPrefix = args.split("\\s+")[0];
        SharedVariables.commandPrefix = newPrefix;
        return P + "Prefix changed to: §c" + newPrefix;
    }

    private static String vclipCommand(String args) {
        if (args.isEmpty()) {
            return P + "§cUsage: vclip <blocks>";
        }
        try {
            double blocks = Double.parseDouble(args.split("\\s+")[0]);
            ClipUtils.vClip(blocks);
            return P + "VClip: §c" + blocks + " §7blocks";
        } catch (NumberFormatException e) {
            return P + "§cInvalid number";
        }
    }

    private static String hclipCommand(String args) {
        if (args.isEmpty()) {
            return P + "§cUsage: hclip <blocks>";
        }
        try {
            double blocks = Double.parseDouble(args.split("\\s+")[0]);
            ClipUtils.hClip(blocks);
            return P + "HClip: §c" + blocks + " §7blocks";
        } catch (NumberFormatException e) {
            return P + "§cInvalid number";
        }
    }

    private static String pluginsCommand(String args) {
        PluginScanner.startScan();
        return P + "Scanning plugins...";
    }

    private static double evaluateExpression(String expression) {
        expression = expression.replaceAll("\\s+", "");
        return parseExpression(expression, new int[]{0});
    }

    private static double parseExpression(String expr, int[] pos) {
        double result = parseTerm(expr, pos);
        while (pos[0] < expr.length()) {
            char op = expr.charAt(pos[0]);
            if (op == '+') { pos[0]++; result += parseTerm(expr, pos); }
            else if (op == '-') { pos[0]++; result -= parseTerm(expr, pos); }
            else break;
        }
        return result;
    }

    private static double parseTerm(String expr, int[] pos) {
        double result = parseFactor(expr, pos);
        while (pos[0] < expr.length()) {
            char op = expr.charAt(pos[0]);
            if (op == '*') { pos[0]++; result *= parseFactor(expr, pos); }
            else if (op == '/') { pos[0]++; result /= parseFactor(expr, pos); }
            else break;
        }
        return result;
    }

    private static double parseFactor(String expr, int[] pos) {
        return parsePower(expr, pos);
    }

    private static double parsePower(String expr, int[] pos) {
        double base = parseUnary(expr, pos);
        if (pos[0] < expr.length() && expr.charAt(pos[0]) == '^') {
            pos[0]++;
            return Math.pow(base, parsePower(expr, pos));
        }
        return base;
    }

    private static double parseUnary(String expr, int[] pos) {
        if (pos[0] < expr.length() && expr.charAt(pos[0]) == '-') {
            pos[0]++;
            return -parseUnary(expr, pos);
        }
        return parsePrimary(expr, pos);
    }

    private static double parsePrimary(String expr, int[] pos) {
        if (pos[0] < expr.length() && expr.charAt(pos[0]) == '(') {
            pos[0]++;
            double result = parseExpression(expr, pos);
            if (pos[0] < expr.length() && expr.charAt(pos[0]) == ')') pos[0]++;
            return result;
        }
        StringBuilder sb = new StringBuilder();
        while (pos[0] < expr.length() && (Character.isDigit(expr.charAt(pos[0])) || expr.charAt(pos[0]) == '.')) {
            sb.append(expr.charAt(pos[0]++));
        }
        return Double.parseDouble(sb.toString());
    }

    @FunctionalInterface
    public interface CommandHandler {
        String execute(String args);
    }
}
