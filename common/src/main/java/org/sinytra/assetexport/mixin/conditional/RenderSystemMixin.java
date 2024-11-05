package org.sinytra.assetexport.mixin.conditional;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.sinytra.assetexport.CommonClass;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Debug(export = true)
@SuppressWarnings("FieldMayBeFinal")
@Mixin(value = RenderSystem.class, remap = false)
public class RenderSystemMixin {
    @Shadow
    private static Thread renderThread;

    private static final Tesselator $RENDER_THREAD_TESSELATOR = new Tesselator(1536);

    private static Matrix4f $projectionMatrix = new Matrix4f();
    private static Matrix4f savedProjectionMatrix = new Matrix4f();
    private static VertexSorting $vertexSorting = VertexSorting.DISTANCE_TO_ORIGIN;
    private static VertexSorting $savedVertexSorting = VertexSorting.DISTANCE_TO_ORIGIN;
    private static final Matrix4fStack $modelViewStack = new Matrix4fStack(16);
    private static Matrix4f $modelViewMatrix = new Matrix4f();
    private static Matrix4f $textureMatrix = new Matrix4f();
    private static final int[] $shaderTextures = new int[12];
    private static final float[] $shaderColor = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
    private static float $shaderGlintAlpha = 1.0F;
    private static float $shaderFogStart;
    private static float $shaderFogEnd = 1.0F;
    private static final float[] $shaderFogColor = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
    private static FogShape $shaderFogShape = FogShape.SPHERE;
    private static final Vector3f[] $shaderLightDirections = new Vector3f[2];
    private static float $shaderGameTime;
    private static float $shaderLineWidth = 1.0F;

    private static ShaderInstance $shader;

    @Overwrite
    public static boolean isOnRenderThread() {
        var current = Thread.currentThread();
        return current == renderThread || current == CommonClass.mainTh;
    }

}
