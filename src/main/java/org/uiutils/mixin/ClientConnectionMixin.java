package org.uiutils.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiutils.SharedVariables;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    // called when sending any packet
    @Inject(at = @At("HEAD"), method = "sendImmediately", cancellable = true)
    public void sendImmediately(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        // checks for if packets should be sent and if the packet is a gui related packet
        if (!SharedVariables.sendUIPackets && (packet instanceof ClickSlotC2SPacket || packet instanceof ButtonClickC2SPacket)) {
            ci.cancel();
            return;
        }

        // checks for if packets should be delayed and if the packet is a gui related packet and is added to a list
        if (SharedVariables.delayUIPackets && (packet instanceof ClickSlotC2SPacket || packet instanceof ButtonClickC2SPacket)) {
            SharedVariables.delayedUIPackets.add(packet);
            ci.cancel();
        }

        // cancels sign update packets if sign editing is disabled and re-enables sign editing
        if (!SharedVariables.shouldEditSign && (packet instanceof UpdateSignC2SPacket)) {
            SharedVariables.shouldEditSign = true;
            ci.cancel();
        }
    }
}
