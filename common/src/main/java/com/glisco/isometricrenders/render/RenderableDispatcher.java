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

import com.glisco.isometricrenders.mixin.access.FramebufferAccessor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;

import java.util.function.Consumer;

public class RenderableDispatcher {

    /**
     * Renders the given renderable into the current framebuffer,
     * with the projection matrix adjusted to compensate for the buffer's
     * aspect ratio
     *
     * @param renderable  The renderable to draw
     * @param aspectRatio The aspect ratio of the current framebuffer
     * @param tickDelta   The tick delta to use
     */
    public static void drawIntoActiveFramebuffer(Renderable<?> renderable, float aspectRatio, float tickDelta, Consumer<Matrix4fStack> transformer) {

        renderable.prepare();

        // Prepare model view matrix
        final var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();

        transformer.accept(modelViewStack);

        renderable.properties().applyToViewMatrix(modelViewStack);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.backupProjectionMatrix();
        Matrix4f projectionMatrix = new Matrix4f().setOrtho(-aspectRatio, aspectRatio, -1, 1, -1000, 3000);

        // Unproject to get the camera position for vertex sorting
        var camPos = new Vector4f(0, 0, 0, 1);
        camPos.mul(new Matrix4f(projectionMatrix).invert()).mul(new Matrix4f(modelViewStack).invert());
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.byDistance(-camPos.x, -camPos.y, -camPos.z));

//        ExampleMod.beginRenderableDraw();

        RenderSystem.runAsFancy(() -> {
            // Emit untransformed vertices
            renderable.emitVertices(
                    new PoseStack(),
                    Minecraft.getInstance().renderBuffers().bufferSource(),
                    tickDelta
            );

            // --> Draw
            renderable.draw(modelViewStack);
        });

//        ExampleMod.endRenderableDraw();

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();

        renderable.cleanUp();
        RenderSystem.restoreProjectionMatrix();
    }

    /**
     * Directly draws the given renderable into a {@link NativeImage} at the given resolution.
     * This method is essentially just a shorthand for {@code copyFramebufferIntoImage(drawIntoTexture(renderable, size))}
     *
     * @param renderable The renderable to draw
     * @param size       The resolution to render at
     * @return The created image
     */
    public static NativeImage drawIntoImage(Renderable<?> renderable, float tickDelta, int size) {
        return copyFramebufferIntoImage(drawIntoTexture(renderable, tickDelta, size));
    }

    /**
     * Draws the given renderable into a new framebuffer. The FBO and depth attachment
     * are deleted afterwards to save video memory, only the color attachment remains
     *
     * @param renderable The renderable to render
     * @param size       The resolution to render aat
     * @return The framebuffer object holding the pointer to the color attachment
     */
    @SuppressWarnings("ConstantConditions")
    public static RenderTarget drawIntoTexture(Renderable<?> renderable, float tickDelta, int size) {
        final var framebuffer = new TextureTarget(size, size, true, Minecraft.ON_OSX);

        RenderSystem.enableBlend();
        RenderSystem.clear(16640, Minecraft.ON_OSX);

        framebuffer.setClearColor(0, 0, 0, 0);
        framebuffer.clear(Minecraft.ON_OSX);

        framebuffer.bindWrite(true);
        drawIntoActiveFramebuffer(renderable, 1, tickDelta, matrixStack -> {});
        framebuffer.unbindWrite();

        // Release depth attachment and FBO to save on VRAM - we only need
        // the color attachment texture to later turn into an image
        final var accessor = (FramebufferAccessor) framebuffer;
        TextureUtil.releaseTextureId(framebuffer.getDepthTextureId());
        accessor.isometric$setDepthAttachment(-1);

        GlStateManager._glDeleteFramebuffers(accessor.isometric$getFbo());
        accessor.isometric$setFbo(-1);

        return framebuffer;
    }

    /**
     * Copies the given framebuffer's color attachment from video
     * memory in to system memory, wrapped in a {@link NativeImage}
     *
     * @param framebuffer The framebuffer to copy
     * @return The created image
     */
    public static NativeImage copyFramebufferIntoImage(RenderTarget framebuffer) {
        final NativeImage img = new NativeImage(framebuffer.width, framebuffer.height, false);

        // This call internally binds the buffer's color attachment texture
        framebuffer.bindRead();

        // This method gets the pixels from the currently bound texture
        img.downloadTexture(0, false);
        img.flipY();

        framebuffer.destroyBuffers();

        return img;
    }
}
