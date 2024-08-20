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
package com.glisco.isometricrenders.property;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4fStack;

public class DefaultPropertyBundle implements PropertyBundle {

    private static final DefaultPropertyBundle INSTANCE = new DefaultPropertyBundle();

    public final IntProperty scale = IntProperty.of(100, 0, 500);
    public final IntProperty rotation = IntProperty.of(135, 0, 360).withRollover();
    public final IntProperty slant = IntProperty.of(30, -90, 90);
    public final IntProperty lightAngle = IntProperty.of(-45, -45, 45);

    public final IntProperty xOffset = IntProperty.of(0, Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2);
    public final IntProperty yOffset = IntProperty.of(0, Integer.MIN_VALUE / 2, Integer.MAX_VALUE / 2);

    public final IntProperty rotationSpeed = IntProperty.of(0, 0, 100);
    protected float rotationOffset = 0;
    protected boolean rotationOffsetUpdated = false;

    public DefaultPropertyBundle() {
//        ClientRenderCallback.EVENT.register(client -> {
//            this.rotationOffsetUpdated = false;
//        });
    }

    @Override
    public void applyToViewMatrix(Matrix4fStack modelViewStack) {
        final float scale = this.scale.get() / 100f;
        modelViewStack.scale(scale, scale, scale);

        modelViewStack.translate(this.xOffset.get() / 26000f, this.yOffset.get() / -26000f, 0);

        modelViewStack.rotate(Axis.XP.rotationDegrees(this.slant.get()));
        modelViewStack.rotate(Axis.YP.rotationDegrees(this.rotation.get()));

        this.updateAndApplyRotationOffset(modelViewStack);
    }

    public float rotationOffset() {
        return this.rotationOffset;
    }

    protected void updateAndApplyRotationOffset(Matrix4fStack modelViewStack) {
        if (rotationSpeed.get() != 0) {
            if (!this.rotationOffsetUpdated) {
                rotationOffset += Minecraft.getInstance().getTimer().getGameTimeDeltaTicks() * rotationSpeed.get() * .1f;
                this.rotationOffsetUpdated = true;
            }
            modelViewStack.rotate(Axis.YP.rotationDegrees(rotationOffset));
        } else {
            rotationOffset = 0;
        }
    }

    public static DefaultPropertyBundle get() {
        return INSTANCE;
    }
}
