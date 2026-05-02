package org.sinytra.wiki.exporter.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SimpleItemRenderer {
    private static final int PACKED_LIGHT = 15728880;

    private final ProjectionMatrixBuffer itemsProjectionMatrixBuffer = new ProjectionMatrixBuffer(
        "items"
    );
    private final Projection projection;

    private final RenderTarget renderTarget;
    private final MultiBufferSource.BufferSource bufferSource;
    private final int scale;
    private final ItemModelResolver resolver;

    public SimpleItemRenderer(RenderTarget renderTarget, MultiBufferSource.BufferSource bufferSource, int scale) {
        this.renderTarget = renderTarget;
        this.bufferSource = bufferSource;
        this.scale = scale;
        this.resolver = new ItemModelResolver(Minecraft.getInstance().getModelManager());
        this.projection = new Projection();
        this.projection.setupOrtho(
            -10000.0F, 10000.0F, scale, scale, true
        );;
    }

    public void renderItem(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.outputColorTextureOverride = this.renderTarget.getColorTextureView();
        RenderSystem.outputDepthTextureOverride = this.renderTarget.getDepthTextureView();
        int scale = this.scale;

        ItemDisplayContext context = ItemDisplayContext.GUI;
        TrackingItemStackRenderState trackingState = new TrackingItemStackRenderState();
        minecraft
            .getItemModelResolver()
            .updateForTopItem(trackingState, stack, context, null, null, 0);
        boolean isGui3D = trackingState.usesBlockLight();
        Lighting.Entry lighting = isGui3D ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT;

        RenderSystem.setupDefaultState();
        RenderSystem.setProjectionMatrix(this.itemsProjectionMatrixBuffer.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);
        minecraft.gameRenderer.getLighting().setupFor(lighting);

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(scale / 2.0F, scale / 2.0F, 0.0F);
        poseStack.scale(scale, -scale, scale);

        ItemStackRenderState state = new ItemStackRenderState();
        this.resolver.updateForTopItem(state, stack, context, null, null, 0);
        SubmitNodeCollector storage = minecraft.gameRenderer.getSubmitNodeStorage();
        state.submit(poseStack, storage, PACKED_LIGHT, OverlayTexture.NO_OVERLAY, 0);
        minecraft.gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
        this.bufferSource.endBatch();

        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
    }
}
