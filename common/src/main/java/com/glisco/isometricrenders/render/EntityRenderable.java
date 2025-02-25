package com.glisco.isometricrenders.render;

import com.glisco.isometricrenders.property.DefaultPropertyBundle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.joml.Matrix4fStack;

public class EntityRenderable extends DefaultRenderable<DefaultPropertyBundle> {
    private final Entity entity;
    private final int res;

    public EntityRenderable(Entity entity, int res) {
        this.entity = entity;
        this.res = res;
    }

    @Override
    public void prepare() {
        entity.tickCount = 0;
        entity.setXRot(0);
        entity.xRotO = 0;
        entity.setYRot(0);
        entity.yRotO = 0;

        if (entity instanceof LivingEntity living) {
            living.yBodyRot = 0;
            living.yBodyRotO = 0;
            living.yHeadRot = 0;
            living.yHeadRotO = 0;
        }

        Entity cameraEntity = Minecraft.getInstance().getCameraEntity() == null ? Minecraft.getInstance().player : Minecraft.getInstance().getCameraEntity();
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        camera.setup(
                Minecraft.getInstance().level, cameraEntity, !Minecraft.getInstance().options.getCameraType().isFirstPerson(), Minecraft.getInstance().options.getCameraType().isMirrored(), 1f
        );
        Minecraft.getInstance().getEntityRenderDispatcher().prepare(Minecraft.getInstance().level, camera, null);
    }

    @Override
    public void emitVertices(PoseStack matrices, MultiBufferSource vertexConsumers, float tickDelta) {
        var renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        renderManager.render(
                entity,
                0, 0, 0, 0, tickDelta,
                matrices,
                vertexConsumers,
                LightTexture.FULL_BRIGHT
        );
    }

    @Override
    public DefaultPropertyBundle properties() {
        return new DefaultPropertyBundle() {
            @Override
            public void applyToViewMatrix(Matrix4fStack modelViewStack) {
                var dim = entity.getDimensions(Pose.STANDING);

                this.slant.set(10);
                var slant = this.slant.get();
                this.rotation.set(10);
                var rot = this.rotation.get();
                double w = dim.width(), l = dim.width(), h = dim.height();

                l = l + h * Math.cos(Math.toRadians(90 - slant));
                h = h * Math.sin(Math.toRadians(90 - slant));

                double wi = w, li = l;
                double rotCos = Math.cos(Math.toRadians(90 - rot));
                w = wi * rotCos + li * Math.cos(Math.toRadians(rot));
                l = li * rotCos + wi * Math.cos(Math.toRadians(rot));

                var max = Math.max(Math.max(w, l), h);

                modelViewStack.translate(
                        0f,
                        -1f,
                        0f
                );

                modelViewStack.rotate(Axis.XP.rotationDegrees(slant));
                modelViewStack.rotate(Axis.YP.rotationDegrees(rot));

                modelViewStack.scale(1/(float)max, 1/(float)max, 1/(float)max);
            }
        };
    }
}
