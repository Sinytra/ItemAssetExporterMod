package org.sinytra.assetexport.mixin;

import net.minecraft.client.Minecraft;
import org.sinytra.assetexport.CommonClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "onGameLoadFinished", at = @At("HEAD"))
    private void onResourceReloadComplete(CallbackInfo ci) {
        CommonClass.startRender();
    }

    @Inject(at = @At("HEAD"), method = "runTick", cancellable = true)
    private void captureTick(boolean renderLevel, CallbackInfo ci) {
        if (CommonClass.mainThread != null) {
            CommonClass.mainThread.run();
            ci.cancel();
        }
    }
}
