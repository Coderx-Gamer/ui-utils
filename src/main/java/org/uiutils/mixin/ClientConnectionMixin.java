package org.uiutils.mixin;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiutils.SharedVariables;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(at = @At("HEAD"), method = "sendImmediately", cancellable = true)
    public void sendImmediately(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> callback, CallbackInfo ci) {
        if (!SharedVariables.sendUIPackets && (packet instanceof ClickSlotC2SPacket)) {
            ci.cancel();
        }
        if (SharedVariables.delayUIPackets && (packet instanceof ClickSlotC2SPacket)) {
            SharedVariables.delayedUIPackets.add(packet);
            ci.cancel();
        }
        if (packet instanceof UpdateSignC2SPacket && !SharedVariables.shouldEditSign) {
            SharedVariables.shouldEditSign = true;
            ci.cancel();
        }
    }
}
