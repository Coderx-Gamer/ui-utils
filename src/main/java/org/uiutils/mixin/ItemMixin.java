package org.uiutils.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(at = @At("TAIL"), method = "use", cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (user.getStackInHand(hand).getItem().equals(Items.SUGAR)) {
            if (world.isClient) {
                try {
                    Clip clip = AudioSystem.getClip();
                    BufferedInputStream stream = new BufferedInputStream(new FileInputStream(new File("sniff.wav")));
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(stream);
                    clip.open(inputStream);
                    clip.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.world.addParticle(ParticleTypes.CLOUD, user.getX(), user.getY() + 1.4, user.getZ(), 0, 0, 0);
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, mc.player.getStatusEffect(StatusEffects.NAUSEA) == null ? 20 : mc.player.getStatusEffect(StatusEffects.NAUSEA).getDuration() + 400, 1));
                mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(0, 0, 36 + mc.player.getInventory().selectedSlot, 0, SlotActionType.PICKUP, ItemStack.EMPTY, new Int2ObjectArrayMap<>()));
                mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(0, 0, 36 + mc.player.getInventory().selectedSlot, 1, SlotActionType.PICKUP, ItemStack.EMPTY, new Int2ObjectArrayMap<>()));
                mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(0, 0, 36 + mc.player.getInventory().selectedSlot, 50, SlotActionType.SWAP, ItemStack.EMPTY, new Int2ObjectArrayMap<>()));
                mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(0, 0, 36 + mc.player.getInventory().selectedSlot, 0, SlotActionType.PICKUP, ItemStack.EMPTY, new Int2ObjectArrayMap<>()));
                cir.setReturnValue(TypedActionResult.consume(user.getStackInHand(hand)));
            }
        }
    }
}
