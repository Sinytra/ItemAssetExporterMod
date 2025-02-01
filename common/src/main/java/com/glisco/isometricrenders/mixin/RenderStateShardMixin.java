package com.glisco.isometricrenders.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.RenderStateShard;
import org.sinytra.assetexport.CommonClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderStateShard.class)
public class RenderStateShardMixin {

    @ModifyExpressionValue(method = {"lambda$static$38", "lambda$static$42", "lambda$static$50", "method_62272", "method_34555", "method_29377"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private static RenderTarget injectProperRenderTarget(RenderTarget original) {
        if (CommonClass.mainTargetOverride != null) {
            return CommonClass.mainTargetOverride;
        }
        return original;
    }
}
