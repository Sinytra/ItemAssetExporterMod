package org.sinytra.assetexport.mixin.conditional;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.sinytra.assetexport.CommonClass;
import org.sinytra.assetexport.mixin.access.RenderSystemAccess;
import org.sinytra.assetexport.render.DumpProgressRenderer;
import org.sinytra.assetexport.render.OffThreadDumper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executors;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Unique
    private boolean replaced;

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void cancelRender(DeltaTracker tracker, boolean renderLevel, CallbackInfo ci) {
        if (CommonClass.render) {
            if (!replaced) {
                replaced = true;

                var window = Minecraft.getInstance().getWindow();
                CommonClass.mainThread = new DumpProgressRenderer(window);
                CommonClass.mainTh = Thread.currentThread();

                new Thread("Dump thread") {
                    {
                        setDaemon(true);
                    }

                    @Override
                    public void run() {
                        RenderSystemAccess.setRenderThread(Thread.currentThread());

                        var dumper = new OffThreadDumper(this);

                        // copied from Window#<init>, i can't be arsed to find out the inlined constants
                        GLFW.glfwDefaultWindowHints();
                        GLFW.glfwWindowHint(139265, 196609);
                        GLFW.glfwWindowHint(139275, 221185);
                        GLFW.glfwWindowHint(139266, 3);
                        GLFW.glfwWindowHint(139267, 2);
                        GLFW.glfwWindowHint(139272, 204801);
                        GLFW.glfwWindowHint(139270, 1);

                        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
                        var newWindow = glfwCreateWindow(window.getWidth(), window.getHeight(), "Minecraft: background render", 0L, window.getWindow());
                        glfwMakeContextCurrent(newWindow);
                        GL.createCapabilities();

                        // TODO - this is CURSED. we need to figure out the joining as we're starting to spawn too many threads
                        Executors.newSingleThreadExecutor().execute(() -> CommonClass.queueTasks(dumper));

                        dumper.run();
                    }
                }.start();
            }
            ci.cancel();
        }
    }
}
