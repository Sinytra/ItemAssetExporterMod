/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.glisco.isometricrenders.render;

import com.glisco.isometricrenders.mixin.access.AnimatedTextureAccessor;
import com.glisco.isometricrenders.mixin.access.ItemRenderStateAccessor;
import com.glisco.isometricrenders.mixin.access.LayerRenderStateAccessor;
import com.glisco.isometricrenders.mixin.access.SpriteContentsAccessor;
import com.glisco.isometricrenders.property.DefaultPropertyBundle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fStack;

import java.util.Objects;
import java.util.stream.Stream;

public class ItemRenderable extends DefaultRenderable<DefaultPropertyBundle> {
    private static final ItemStackRenderState RENDER_STATE = new ItemStackRenderState();
    private static final DefaultPropertyBundle PROPERTIES = new DefaultPropertyBundle() {
        @Override
        public void applyToViewMatrix(Matrix4fStack modelViewStack) {
            final float scale = (this.scale.get() / 100f) * (RENDER_STATE.isGui3d() ? 2f : 1.75f);
            modelViewStack.scale(scale, scale, scale);

            modelViewStack.translate(this.xOffset.get() / 26000f, this.yOffset.get() / -26000f, 0);

            modelViewStack.rotate(Axis.XP.rotationDegrees(this.slant.get()));
            var bruhMatrices = new PoseStack();
            RENDER_STATE.transform().apply(false, bruhMatrices);
            modelViewStack.mul(bruhMatrices.last().pose());
            modelViewStack.rotate(Axis.YP.rotationDegrees(this.rotation.get()));

            this.updateAndApplyRotationOffset(modelViewStack);
        }
    };

    static {
        PROPERTIES.slant.setDefaultValue(0).setToDefault();
        PROPERTIES.rotation.setDefaultValue(0).setToDefault();
    }

    private final ItemStack stack;

    public ItemRenderable(ItemStack stack) {
        this.stack = stack;
    }

    public int getAnimationTicks() {
        return getSprites().map(TextureAtlasSprite::contents).distinct()
            .map(c -> ((SpriteContentsAccessor) c).iae$getAnimatedTexture())
            .filter(Objects::nonNull)
            .mapToInt(t -> ((AnimatedTextureAccessor) t).iae$getFrames().stream().mapToInt(SpriteContents.FrameInfo::time).sum())
            .reduce(1, ItemRenderable::lcm);
    }

    public Stream<TextureAtlasSprite> getSprites() {
        prepare();

        ItemStackRenderState.LayerRenderState layer = ((ItemRenderStateAccessor) RENDER_STATE).invokeFirstLayer();
        BakedModel bakedModel = ((LayerRenderStateAccessor) layer).getModel();
        if (bakedModel == null) {
            return Stream.empty();
        }

        var quads = bakedModel.getQuads(null, null, RandomSource.create(1L));
        return quads.stream().map(BakedQuad::getSprite).distinct();
    }

    private static int lcm(int number1, int number2) {
        if (number1 == 0 || number2 == 0) {
            return 0;
        }
        int absNumber1 = Math.abs(number1);
        int absNumber2 = Math.abs(number2);
        int absHigherNumber = Math.max(absNumber1, absNumber2);
        int absLowerNumber = Math.min(absNumber1, absNumber2);
        int lcm = absHigherNumber;
        while (lcm % absLowerNumber != 0) {
            lcm += absHigherNumber;
        }
        return lcm;
    }

    @Override
    public void prepare() {
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(
            RENDER_STATE,
            this.stack,
            ItemDisplayContext.GUI,
            false,
            Minecraft.getInstance().level,
            null,
            0
        );
    }

    @Override
    public void emitVertices(PoseStack matrices, MultiBufferSource vertexConsumers, float tickDelta) {
        ((ItemRenderStateAccessor) RENDER_STATE).isometric$setTransformationMode(ItemDisplayContext.NONE);
        RENDER_STATE.render(matrices, vertexConsumers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    @Override
    public void cleanUp() {
        RENDER_STATE.clear();
    }

    @Override
    public DefaultPropertyBundle properties() {
        return PROPERTIES;
    }
}
