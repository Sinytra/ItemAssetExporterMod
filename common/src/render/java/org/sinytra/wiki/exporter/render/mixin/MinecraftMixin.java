package org.sinytra.wiki.exporter.render.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
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
        Minecraft minecraft = (Minecraft) (Object) this;
        Screen oldScreen = minecraft.screen;
        // Trigger WorldLoader.load to setup item components, necessary for rendering
        CreateWorldScreen.testWorld(minecraft, () -> minecraft.setScreen(oldScreen));

        WikiDataExporter.runModule(WikiRenderModuleFactory.NAME);
    }
}
