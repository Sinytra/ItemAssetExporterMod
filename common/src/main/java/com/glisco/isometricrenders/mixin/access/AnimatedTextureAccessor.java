package com.glisco.isometricrenders.mixin.access;

import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SpriteContents.AnimatedTexture.class)
public interface AnimatedTextureAccessor {
    @Accessor("frames")
    List<SpriteContents.FrameInfo> iae$getFrames();
}
