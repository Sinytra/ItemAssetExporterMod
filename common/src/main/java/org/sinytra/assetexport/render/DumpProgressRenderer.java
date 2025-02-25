package org.sinytra.assetexport.render;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.sinytra.assetexport.CommonClass;
import org.sinytra.assetexport.ProgressTracker;

import java.util.concurrent.TimeUnit;

import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;

public class DumpProgressRenderer implements Runnable {
    // Cap our window at 100FPS
    private static final long MINFRAMETIME = TimeUnit.MILLISECONDS.toNanos(10);

    private final Window window;
    private final RenderTarget target;
    private final RenderBuffers buffers;
    private final Font font;

    public DumpProgressRenderer(Window window) {
        this.window = window;
        this.target = new MainTarget(window.getWidth(), window.getHeight());
        this.buffers = new RenderBuffers(1);
        this.font = Minecraft.getInstance().font;

        this.target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        target.clear(Minecraft.ON_OSX);
    }

    private long nextFrameTime = 0;

    @Override
    public void run() {
        glfwMakeContextCurrent(window.getWindow());
        GL.createCapabilities();

        glfwSetWindowTitle(window.getWindow(), "Minecraft Asset Export");

        // Wait for one frame to be complete before swapping; enable vsync in other words.
        glfwSwapInterval(1);
        try {
            if (Minecraft.getInstance().isRunning() && !window.shouldClose()) {
                long nt;
                if ((nt = System.nanoTime()) < nextFrameTime) {
                    return;
                }
                nextFrameTime = nt + MINFRAMETIME;

                var window = Minecraft.getInstance().getWindow();

                RenderSystem.clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
                target.bindWrite(true);
                RenderSystem.enableCull();

                RenderSystem.clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
                RenderSystem.clear(256, Minecraft.ON_OSX);

                Matrix4f matrix4f = new Matrix4f()
                        .setOrtho(
                                0.0F,
                                (float) ((double) window.getWidth() / window.getGuiScale()),
                                (float) ((double) window.getHeight() / window.getGuiScale()),
                                0.0F,
                                1000.0F,
                                21000.0F
                        );
                RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
                Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
                matrix4fstack.pushMatrix();
                matrix4fstack.translation(0.0F, 0.0F, -11000.0F);
                RenderSystem.applyModelViewMatrix();
                Lighting.setupFor3DItems();
                GuiGraphics guigraphics = new GuiGraphics(Minecraft.getInstance(), buffers.bufferSource());

                render(guigraphics);


                guigraphics.flush();
                matrix4fstack.popMatrix();
                RenderSystem.applyModelViewMatrix();

                target.unbindWrite();
                target.blitToScreen(window.getWidth(), window.getHeight());

                Tesselator.getInstance().clear();
                GLFW.glfwSwapBuffers(window.getWindow());
                glfwPollEvents();
            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void render(GuiGraphics graphics) {
        int width = (int)(window.getWidth() / window.getGuiScale());
        int y = 30;

        if (ProgressTracker.start == 0 && CommonClass.loadWorld) {
            graphics.drawCenteredString(font, "Loading render world...", width / 2, y, 0xffffff);
        } else {
            graphics.drawCenteredString(font, "Running asset dump...", width / 2, y, 0xffffff);
        }

        y += font.lineHeight + 5;

        if (ProgressTracker.start > 0) {
            graphics.pose().pushPose();
            graphics.pose().scale(0.5f, 0.5f, 1f);
            var text = "Time elapsed: " + (System.currentTimeMillis() - ProgressTracker.start) / 1000 + " seconds";
            graphics.drawString(font, text, width - font.width(text) / 2, y * 2, 0xffffff);
            graphics.pose().popPose();
        }

        y += 30 + font.lineHeight / 2 + 10;

        if (ProgressTracker.currentRender != null) {
            graphics.drawCenteredString(font, "Currently rendering: " + ProgressTracker.currentRender.representAsString(), width / 2, y, 0xffffff);
        }

        y += 20 + font.lineHeight;

        int minX = 0;
        if (ProgressTracker.generated != null) {
            var text = "Generated assets: " + ProgressTracker.generated.done().get() + "/" + ProgressTracker.generated.target();
            graphics.drawString(font, text, 40, y + 1, 0xffffff);
            minX = 40 + font.width(text) + 10;
            drawProgressBar(graphics, minX, y, minX + 80, y + 10, 1f, ProgressTracker.generated.get());

            y += 15;
        }

        if (ProgressTracker.dumped != null) {
            var text = "Dumped assets: " + ProgressTracker.dumped.done().get() + "/" + ProgressTracker.dumped.target();
            graphics.drawString(font, text, 40, y + 1, 0xffffff);
            drawProgressBar(graphics, minX, y, minX + 80, y + 10, 1f, ProgressTracker.dumped.get());
        }
    }

    private void drawProgressBar(GuiGraphics guiGraphics, int minX, int minY, int maxX, int maxY, float partialTick, float currentProgress) {
        int i = Mth.ceil((float)(maxX - minX - 2) * currentProgress);
        int j = Math.round(partialTick * 255.0F);
        int k = FastColor.ARGB32.color(j, 255, 255, 255);
        guiGraphics.fill(minX + 2, minY + 2, minX + i, maxY - 2, k);
        guiGraphics.fill(minX + 1, minY, maxX - 1, minY + 1, k);
        guiGraphics.fill(minX + 1, maxY, maxX - 1, maxY - 1, k);
        guiGraphics.fill(minX, minY, minX + 1, maxY, k);
        guiGraphics.fill(maxX, minY, maxX - 1, maxY, k);
    }
}
