package org.sinytra.assetexport.mixin.conditional;

import com.mojang.blaze3d.vertex.Tesselator;
import org.sinytra.assetexport.CommonClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Tesselator.class)
public class TesselatorMixin {
    @Shadow
    private static Tesselator instance;

    private static Tesselator $instance;

    @Overwrite
    public static void init() {
        if (instance != null) {
            throw new IllegalStateException("Tesselator has already been initialized");
        } else {
            instance = new Tesselator();
            $instance = new Tesselator();
        }
    }

    @Overwrite
    public static Tesselator getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Tesselator has not been initialized");
        } else {
            return Thread.currentThread() == CommonClass.mainTh ? $instance : instance;
        }
    }
}
