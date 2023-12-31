package org.uiutils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiutils.MainClient;
import org.uiutils.SharedVariables;

@Mixin(ClientCommonNetworkHandler.class)
public abstract class ClientCommonNetworkHandlerMixin {
    @Shadow
    @Final
    protected MinecraftClient client;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Inject(at = @At("HEAD"), method = "onResourcePackSend", cancellable = true)
    public void onResourcePackSend(ResourcePackSendS2CPacket packet, CallbackInfo ci) {
        if (SharedVariables.bypassResourcePack && (packet.required() || SharedVariables.resourcePackForceDeny)) {
            this.sendPacket(new ResourcePackStatusC2SPacket(MinecraftClient.getInstance().getSession().getUuidOrNull(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
            this.sendPacket(new ResourcePackStatusC2SPacket(MinecraftClient.getInstance().getSession().getUuidOrNull(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
            MainClient.LOGGER.info(
                    "[UI Utils]: Required Resource Pack Bypassed, Message: " +
                            (packet.prompt() == null ? "<no message>" : packet.prompt().getString()) +
                            ", URL: " + (packet.url() == null ? "<no url>" : packet.url())
            );
            ci.cancel();
        }
    }
}
