package com.glisco.isometricrenders.mixin.access;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public
interface LayerRenderStateAccessor {
    @Accessor
    BakedModel getModel();
}
