package org.sinytra.assetexport.mixin.conditional;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.sinytra.assetexport.CommonClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executors;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(at = @At("TAIL"), method = "handleLogin")
    private void triggerRender(ClientboundLoginPacket packet, CallbackInfo ci) {
        if (CommonClass.loadWorld) {
            Executors.newSingleThreadExecutor().execute(() -> CommonClass.queueTasks(CommonClass.renderQueue));
        }
    }
}
