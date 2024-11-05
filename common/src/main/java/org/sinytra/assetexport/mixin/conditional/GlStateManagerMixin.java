package org.sinytra.assetexport.mixin.conditional;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;

import java.util.stream.IntStream;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {
    private static final GlStateManager.BlendState $BLEND = new GlStateManager.BlendState();
    private static final GlStateManager.DepthState $DEPTH = new GlStateManager.DepthState();
    private static final GlStateManager.CullState $CULL = new GlStateManager.CullState();
    private static final GlStateManager.PolygonOffsetState $POLY_OFFSET = new GlStateManager.PolygonOffsetState();
    private static final GlStateManager.ColorLogicState $COLOR_LOGIC = new GlStateManager.ColorLogicState();
    private static final GlStateManager.StencilState $STENCIL = new GlStateManager.StencilState();
    private static final GlStateManager.ScissorState $SCISSOR = new GlStateManager.ScissorState();
    private static int $activeTexture;
    private static final GlStateManager.TextureState[] $TEXTURES = IntStream.range(0, 12)
            .mapToObj(p_157120_ -> new GlStateManager.TextureState())
            .toArray(GlStateManager.TextureState[]::new);
    private static final GlStateManager.ColorMask $COLOR_MASK = new GlStateManager.ColorMask();
}
