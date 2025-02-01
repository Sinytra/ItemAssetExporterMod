package com.glisco.isometricrenders.mixin.access;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemStackRenderState.class)
public interface ItemRenderStateAccessor {
    @Accessor("displayContext")
    void isometric$setTransformationMode(ItemDisplayContext mode);

    @Invoker
    ItemStackRenderState.LayerRenderState invokeFirstLayer();
}
