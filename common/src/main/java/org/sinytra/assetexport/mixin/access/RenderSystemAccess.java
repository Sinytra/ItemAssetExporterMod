package org.sinytra.assetexport.mixin.access;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RenderSystem.class, remap = false)
public interface RenderSystemAccess {
    @Accessor("renderThread")
    static void setRenderThread(Thread renderThread) {

    }
}
