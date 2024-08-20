package com.example.examplemod.mixin;

import com.example.examplemod.CommonClass;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "onGameLoadFinished", at = @At("HEAD"))
    private void onResourceReloadComplete(CallbackInfo ci) {
        CommonClass.runRender();
    }
}
