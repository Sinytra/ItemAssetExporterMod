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

import org.sinytra.assetexport.render.ForwardingBakedModel;
import com.glisco.isometricrenders.property.DefaultPropertyBundle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix4fStack;

public class ItemRenderable extends DefaultRenderable<DefaultPropertyBundle> {

    private static BakedModel currentModel = null;
    private static final DefaultPropertyBundle PROPERTIES = new DefaultPropertyBundle() {
        @Override
        public void applyToViewMatrix(Matrix4fStack modelViewStack) {
            final float scale = (this.scale.get() / 100f) * (currentModel != null && currentModel.isGui3d() ? 2f : 1.75f);
            modelViewStack.scale(scale, scale, scale);

            modelViewStack.translate(this.xOffset.get() / 26000f, this.yOffset.get() / -26000f, 0);

            modelViewStack.rotate(Axis.XP.rotationDegrees(this.slant.get()));
            var bruhMatrices = new PoseStack();
            if (currentModel != null) currentModel.getTransforms().getTransform(ItemDisplayContext.GUI).apply(false, bruhMatrices);
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

    @Override
    public void prepare() {
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        if (this.stack.is(Items.TRIDENT)) {
            currentModel = itemRenderer.getItemModelShaper().getModelManager().getModel(ModelResourceLocation.vanilla("trident", "inventory"));
        } else if (this.stack.is(Items.SPYGLASS)) {
            currentModel = itemRenderer.getItemModelShaper().getModelManager().getModel(ModelResourceLocation.vanilla("spyglass", "inventory"));
        } else {
            currentModel = itemRenderer.getModel(this.stack, Minecraft.getInstance().level, null, 0);
        }
    }

    @Override
    public void emitVertices(PoseStack matrices, MultiBufferSource vertexConsumers, float tickDelta) {
        final var itemRenderer = Minecraft.getInstance().getItemRenderer();
        final var model = itemRenderer.getModel(this.stack, null, null, 0);

        itemRenderer.render(
            this.stack,
            ItemDisplayContext.GUI,
            false,
            matrices,
            vertexConsumers,
            LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY,
            new TransformlessBakedModel(model)
        );
    }

    @Override
    public void cleanUp() {
        currentModel = null;
    }

    @Override
    public DefaultPropertyBundle properties() {
        return PROPERTIES;
    }

    // TODO Might need to be platform specific
    private static class TransformlessBakedModel extends ForwardingBakedModel {
        public TransformlessBakedModel(BakedModel inner) {
            this.wrapped = inner;
        }

        @Override
        public ItemTransforms getTransforms() {
            return ItemTransforms.NO_TRANSFORMS;
        }
    }
}
