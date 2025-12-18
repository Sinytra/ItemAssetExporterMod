package org.sinytra.wiki.exporter.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class SimpleItemRenderer {
    private final MultiBufferSource.BufferSource bufferSource;
    private final int scale;

    public SimpleItemRenderer(MultiBufferSource.BufferSource bufferSource, int scale) {
        this.bufferSource = bufferSource;
        this.scale = scale;
    }

    public void renderItem(ItemStack stack) {
        // Prepare model view matrix
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();

        RenderSystem.applyModelViewMatrix();
        RenderSystem.backupProjectionMatrix();
        Matrix4f projectionMatrix = new Matrix4f().setOrtho(0, scale, scale, 0, -10000, 10000);

        // Unproject to get the camera position for vertex sorting
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.ORTHOGRAPHIC_Z);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, null, null, 0);
        if (model.isGui3d()) {
            Lighting.setupFor3DItems();
        } else {
            Lighting.setupForFlatItems();
        }

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(scale / 2.0F, scale / 2.0F, 0.0F);
        poseStack.scale(scale, -scale, scale);

        itemRenderer.render(
            stack,
            ItemDisplayContext.GUI,
            false,
            poseStack,
            this.bufferSource,
            LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY,
            model
        );

        this.bufferSource.endBatch();

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
    }
}
