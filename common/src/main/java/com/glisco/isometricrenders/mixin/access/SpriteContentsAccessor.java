package com.glisco.isometricrenders.mixin.access;

import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpriteContents.class)
public interface SpriteContentsAccessor {
    @Invoker("getFrameCount")
    int iae$getFrameCount();

    @Accessor("animatedTexture")
    SpriteContents.AnimatedTexture iae$getAnimatedTexture();
}
