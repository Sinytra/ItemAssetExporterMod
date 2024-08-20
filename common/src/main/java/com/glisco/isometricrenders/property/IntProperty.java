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

import net.minecraft.util.Mth;

public class IntProperty extends Property<Integer> {

    private final int max;
    private final int min;
    private final int span;

    private boolean allowRollover = false;

    private IntProperty(int defaultValue, int min, int max) {
        super(defaultValue);

        this.min = min;
        this.max = max;

        this.span = this.max - this.min;
    }

    public static IntProperty of(int defaultValue, int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("'min' must be less than 'max'");
        }

        return new IntProperty(defaultValue, min, max);
    }

    public IntProperty withRollover() {
        this.allowRollover = true;
        return this;
    }

    public void modify(int by) {
        if (allowRollover) {
            this.value += by;
            if (this.value > this.max) this.value -= this.span;
            if (this.value < this.min) this.value += this.span;
        } else {
            this.value = Mth.clamp(this.value + by, this.min, this.max);
        }

        this.invokeListeners();
    }

    public double progress() {
        return (this.value - this.min) / (double) this.span;
    }

    public void setFromProgress(double progress) {
        this.value = (int) Math.round(this.min + progress * this.span);
        this.invokeListeners();
    }

    public int max() {
        return max;
    }

    public int min() {
        return min;
    }
}
