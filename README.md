# UI-Utils

Fabric mod for debugging server GUIs and packets. Supports Minecraft 1.21 - 1.21.x.

---

## Screenshots

<img width="1359" height="713" alt="image" src="https://github.com/user-attachments/assets/b9a97dae-f552-4d80-bbe8-d39e78c3d2f1" />
<img width="1359" height="714" alt="image" src="https://github.com/user-attachments/assets/39b733bd-81ce-497f-888e-d4f038e5cf66" />
<img width="1359" height="717" alt="image" src="https://github.com/user-attachments/assets/4e0d69b3-f2c3-45b3-9e71-bc59f85c28e7" />
<img width="462" height="719" alt="image" src="https://github.com/user-attachments/assets/fd090f99-ec1c-43a1-b2db-7108d0f7ef66" />


---

## Credits

| Role | Author | Link |
|------|--------|------|
| Original Creator | Coderx-Gamer | [GitHub](https://github.com/Coderx-Gamer/ui-utils) |
| Modified by | FrannnDev | [GitHub](https://github.com/FrannnnDev) |

---

## Downloads

| Minecraft | Download |
|-----------|----------|
| 1.21 | [ui_utils-2.4.1-mc1.21.jar](https://github.com/FrannnnDev/ui-utils-advanced/releases/download/v2.4.1/ui_utils-2.4.1-mc1.21.jar) |
| 1.21.1 | [ui_utils-2.4.1-mc1.21.1.jar](https://github.com/FrannnnDev/ui-utils-advanced/releases/download/v2.4.1/ui_utils-2.4.1-mc1.21.1.jar) |
| 1.21.2 | [ui_utils-2.4.1-mc1.21.2.jar](https://github.com/FrannnnDev/ui-utils-advanced/releases/download/v2.4.1/ui_utils-2.4.1-mc1.21.2.jar) |
| 1.21.3 | [ui_utils-2.4.1-mc1.21.3.jar](https://github.com/FrannnnDev/ui-utils-advanced/releases/download/v2.4.1/ui_utils-2.4.1-mc1.21.3.jar) |
| 1.21.4 | [ui_utils-2.4.1-mc1.21.4.jar](https://github.com/FrannnnDev/ui-utils-advanced/releases/download/v2.4.1/ui_utils-2.4.1-mc1.21.4.jar) |
| 1.21.5 | [ui_utils-2.4.1-mc1.21.5.jar](https://github.com/FrannnnDev/ui-utils-advanced/releases/download/v2.4.1/ui_utils-2.4.1-mc1.21.5.jar) |
| 1.21.6 | [ui_utils-2.4.1-mc1.21.6.jar](https://github.com/FrannnnDev/ui-utils-advanced/releases/download/v2.4.1/ui_utils-2.4.1-mc1.21.6.jar) |
| 1.21.7 | [ui_utils-2.4.1-mc1.21.7.jar](https://github.com/FrannnnDev/ui-utils-advanced/releases/download/v2.4.1/ui_utils-2.4.1-mc1.21.7.jar) |

> **Note:** Versions 1.21.8-1.21.11 are not yet supported due to incomplete Fabric yarn mappings.

---

## Installation

1. Install Fabric Loader
2. Drop the mod JAR into your `mods` folder (Fabric API is already included)
3. (Optional) Install [ViaFabricPlus](https://modrinth.com/mod/viafabricplus) for multi-version support

---

## Inventory Overlay (E Menu)

Open any inventory to see the UI-Utils overlay on the left side.

### Info Panel

Located at the top-right of the overlay:

| Field | Description |
|-------|-------------|
| Sync | Current screen handler sync ID used by packets |
| Rev | Current revision number for packet validation |

### Button List

| Button | Description |
|--------|-------------|
| Close without packet | Closes GUI client-side only. Server thinks GUI is still open |
| Desync | Sends close packet to server but keeps GUI open client-side |
| Send packets: true/false | Toggle whether click packets are sent to server |
| Delay packets: true/false | When ON, stores all click packets in queue instead of sending |
| Leave & Send Packets | Sends all queued packets at once, then closes GUI |
| Fabricate packet | Opens external window to create custom packets (not available on Mac) |
| Copy GUI Title JSON | Copies current GUI title as JSON to clipboard |
| Save GUI / Load GUI | Save current GUI state to memory or restore it |
| Clear Queue / Queue: N | Clear all queued packets or show current queue count |
| Resync Inv / Disconnect | Force inventory resync or disconnect from server |
| [-] Spam (xN) [+] | Send all queued packets N times. Use - and + to adjust multiplier |
| Send One / Pop Last | Send first packet from queue or remove last packet without sending |

### Chat Input

At the bottom-left of any GUI there is a chat input field:

- Type messages --> sends to server chat
- Start with `/` --> sends as server command
- Start with `,` --> executes UI-Utils command

---

## Multiplayer Screen

Custom buttons appear at the top of the multiplayer server list.

### Left Side - Resource Pack

| Button | Description |
|--------|-------------|
| Bypass | Bypass required resource packs. Tells server pack was loaded without downloading. Green when active |
| Deny | Force deny all resource packs. Use if Bypass alone still kicks you. Green when active |

### Right Side - Utilities

| Button | Description |
|--------|-------------|
| User | Opens popup to change session username. Temporary until game restart |
| Via+ | Opens ViaFabricPlus version selector. Requires ViaFabricPlus mod installed |

---

## Keybinds

| Key | Action |
|-----|--------|
| V | Load saved GUI (configurable in controls menu) |

---

## Commands

All commands use `,` prefix by default. Change with `,prefix <new>`.

### General

| Command | Usage | Description |
|---------|-------|-------------|
| help | `,help [command]` | List all commands or show help for specific command |
| man | `,man <command>` | Show detailed manual for a command |
| toggleuiutils | `,toggleuiutils` | Toggle UI-Utils overlay on/off |
| prefix | `,prefix [new]` | View or change command prefix |
| echo | `,echo <text>` | Print text to chat (client-side only) |

### Math

| Command | Usage | Description |
|---------|-------|-------------|
| math | `,math <expression>` | Calculate math expression. Supports + - * / ^ () |

Example:
```
,math 2+2*3
--> Result: 8.0
```

### Screen Control

| Command | Usage | Description |
|---------|-------|-------------|
| close | `,close` | Close current screen without sending packet |
| desync | `,desync` | Send close packet but keep GUI open client-side |

### Chat

| Command | Usage | Description |
|---------|-------|-------------|
| chat | `,chat <message>` | Send message or command to server from anywhere |

Example:
```
,chat /spawn
--> Sends /spawn command to server
```

### Server

| Command | Usage | Description |
|---------|-------|-------------|
| joinserver | `,joinserver <ip[:port]>` | Connect to another server |
| plugins | `,plugins` | Scan server plugins via tab-complete exploit |

Plugins output:
- Normal plugins --> red text
- Anticheats/exploit-related --> dark red text

### Screen Management

| Command | Usage | Description |
|---------|-------|-------------|
| screen save | `,screen save <slot>` | Save current screen to named slot |
| screen load | `,screen load <slot>` | Load saved screen from slot |
| screen list | `,screen list` | List all saved screen slots |
| screen info | `,screen info <slot>` | Show info about saved screen |

### Account

| Command | Usage | Description |
|---------|-------|-------------|
| account dump | `,account dump` | Show current username and UUID |
| account set username | `,account set username <name>` | Change session username |
| account set uuid | `,account set uuid <uuid>` | Change session UUID |

Note: Account changes are temporary and reset on game restart.

### Movement

| Command | Usage | Description |
|---------|-------|-------------|
| vclip | `,vclip <blocks>` | Teleport vertically. Positive = up, negative = down |
| hclip | `,hclip <blocks>` | Teleport horizontally in look direction |

Examples:
```
,vclip 5
--> Teleport 5 blocks up

,vclip -3
--> Teleport 3 blocks down

,hclip 10
--> Teleport 10 blocks forward
```

---

## Fabricate Packet

External window for creating custom packets.

### ClickSlotC2SPacket

Used for clicking slots in GUIs.

| Field | Description |
|-------|-------------|
| Sync Id | From overlay info panel |
| Revision | From overlay info panel |
| Slot | Slot number to click (starts at 0) |
| Button | 0 = left click, 1 = right click, 0-8 = hotbar slot, 40 = offhand |
| Action | PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL |
| Times to send | Number of packets to send |
| Delay | Check to add packet to delay queue instead of sending |

### ButtonClickC2SPacket

Used for clicking buttons in server GUIs (enchantment tables, stonecutters, etc).

| Field | Description |
|-------|-------------|
| Sync Id | From overlay info panel |
| Button Id | Button number to click (starts at 0) |
| Times to send | Number of packets to send |
| Delay | Check to add packet to delay queue instead of sending |

---

## Toggle

Type `,toggleuiutils` in chat or any GUI chat input to enable or disable the mod overlay.

---

## Building

### Single Version
```
.\gradlew build
```

### All Versions
```
.\build-all.bat
```

Output JARs will be in `build/libs/` (single) or `build/multiversion/` (all versions).

---

## License

This mod is licensed under CC BY-NC-SA 4.0. See [LICENSE](LICENSE) file.
