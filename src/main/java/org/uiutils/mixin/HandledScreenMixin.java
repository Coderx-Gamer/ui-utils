package org.uiutils.mixin;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiutils.SharedVariables;
import org.uiutils.mixin.accessor.ClientConnectionAccessor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Mixin(HandledScreen.class)
public class HandledScreenMixin extends Screen {
    protected HandledScreenMixin(Text title) {
        super(title);
    }
    //private static boolean rendered = false;

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        this.addDrawableChild(new ButtonWidget(5, 5, 160, 20, Text.of("Close without packet"), (button) -> {
            mc.setScreen(null);
        }));
        this.addDrawableChild(new ButtonWidget(5, 35, 90, 20, Text.of("De-sync"), (button) -> {
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        }));
        this.addDrawableChild(new ButtonWidget(5, 65, 160, 20, Text.of("Send packets: " + SharedVariables.sendUIPackets), (button) -> {
            SharedVariables.sendUIPackets = !SharedVariables.sendUIPackets;
            button.setMessage(Text.of("Send packets: " + SharedVariables.sendUIPackets));
        }));
        this.addDrawableChild(new ButtonWidget(5, 100, 160, 20, Text.of("Delay packets: " + SharedVariables.delayUIPackets), (button) -> {
            SharedVariables.delayUIPackets = !SharedVariables.delayUIPackets;
            button.setMessage(Text.of("Delay packets: " + SharedVariables.delayUIPackets));
            if (!SharedVariables.delayUIPackets && !SharedVariables.delayedUIPackets.isEmpty()) {
                for (Packet<?> packet : SharedVariables.delayedUIPackets) {
                    mc.getNetworkHandler().sendPacket(packet);
                }
                SharedVariables.delayedUIPackets.clear();
            }
        }));
        this.addDrawableChild(new ButtonWidget(5, 130, 160, 20, Text.of("Save GUI"), (button) -> {
            /*File file = new File(mc.runDirectory.getAbsolutePath() + "\\guis");
            File file0 = new File(mc.runDirectory.getAbsolutePath() + "\\guis\\" + textCompoundToString(mc.currentScreen.getTitle()) + ".txt");
            file.mkdirs();
            try {
                file0.createNewFile();
                StringBuilder nbt = new StringBuilder("{Items:[");
                PrintWriter writer = new PrintWriter(file0);
                for (int i = 0; i < mc.player.currentScreenHandler.getStacks().size(); i++) {
                    ItemStack stack = mc.player.currentScreenHandler.getStacks().get(i);
                    String itemName = Registry.ITEM.getId(stack.getItem()).toString();
                    if (stack.getNbt() != null) {
                        if (i != mc.player.currentScreenHandler.getStacks().size()) {
                            if (!stack.getItem().equals(Items.AIR)) {
                                nbt.append("{Slot:" + i + "b,id:\"" + itemName + "\",Count:" + stack.getCount() + "b,tag:" + stack.getNbt() + "}},");
                            }
                        } else {
                            if (!stack.getItem().equals(Items.AIR)) {
                                nbt.append("{Slot:" + i + "b,id:\"" + itemName + "\",Count:" + stack.getCount() + "b,tag:" + stack.getNbt() + "}}]}");
                            }
                        }
                    } else {
                        if (i != mc.player.currentScreenHandler.getStacks().size()) {
                            if (!stack.getItem().equals(Items.AIR)) {
                                nbt.append("{Slot:" + i + "b,id:\"" + itemName + "\",Count:" + stack.getCount() + "b},");
                            }
                        } else {
                            if (!stack.getItem().equals(Items.AIR)) {
                                nbt.append("{Slot:" + i + "b,id:\"" + itemName + "\",Count:" + stack.getCount() + "b}]}");
                            }
                        }
                    }
                }
                writer.write(nbt.toString());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            SharedVariables.storedScreen = client.currentScreen;
            SharedVariables.storedScreenHandler = client.player.currentScreenHandler;
        }));
        this.addDrawableChild(new ButtonWidget(5, 160, 160, 20, Text.of("Delete Item"), (button) -> {
            ScreenHandler screenHandler0 = client.player.currentScreenHandler;
            DefaultedList<Slot> defaultedList0 = screenHandler0.slots;
            int i1 = defaultedList0.size();
            List<ItemStack> list0 = Lists.newArrayListWithCapacity(i1);

            for (Slot slot : defaultedList0) {
                list0.add(slot.getStack().copy());
            }

            Int2ObjectMap<ItemStack> int2ObjectMap0 = new Int2ObjectOpenHashMap<>();

            for(int slot = 0; slot < i1; ++slot) {
                ItemStack itemStack = list0.get(slot);
                ItemStack itemStack2 = (defaultedList0.get(slot)).getStack();
                if (!ItemStack.areEqual(itemStack, itemStack2)) {
                    int2ObjectMap0.put(slot, itemStack2.copy());
                }
            }

            client.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(client.player.currentScreenHandler.syncId, client.player.currentScreenHandler.getRevision(), 0, 50, SlotActionType.SWAP, client.player.currentScreenHandler.getCursorStack().copy(), int2ObjectMap0));
        }));

        this.addDrawableChild(new ButtonWidget(5, 190, 200, 20, Text.of("Disconnect and Send Packets"), (button) -> {
            if (!SharedVariables.delayedUIPackets.isEmpty()) {
                for (int i = 0; i < SharedVariables.delayedUIPackets.size(); i++) {
                    ((ClientConnectionAccessor) client.getNetworkHandler().getConnection()).getChannel().writeAndFlush(SharedVariables.delayedUIPackets.get(i));
                }
                ((ClientConnectionAccessor) client.getNetworkHandler().getConnection()).getChannel().close();
                client.setScreen(new DisconnectedScreen(new MultiplayerScreen(new TitleScreen()), Text.of(""), Text.of("Disconnecting (UI UTILS)")));
                SharedVariables.delayUIPackets = false;
            }
        }));
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.textRenderer.draw(matrices, "Sync Id: " + mc.player.currentScreenHandler.syncId, 200, 5, Color.WHITE.getRGB());
        this.textRenderer.draw(matrices, "Revision: " + mc.player.currentScreenHandler.getRevision(), 200, 35, Color.WHITE.getRGB());
    }

    private static String textCompoundToString(Text textCompound) {
        String[] parts = textCompound.toString().split("'");
        return parts[1];
    }
}
