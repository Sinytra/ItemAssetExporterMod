package org.sinytra.wiki.exporter.render.mixin;

import net.minecraft.client.Minecraft;
import org.sinytra.wiki.exporter.WikiDataExporter;
import org.sinytra.wiki.exporter.render.WikiRenderModuleFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "onGameLoadFinished", at = @At("HEAD"))
    private void onResourceReloadComplete(CallbackInfo ci) {
        WikiDataExporter.runModule(WikiRenderModuleFactory.NAME);
    }
}
