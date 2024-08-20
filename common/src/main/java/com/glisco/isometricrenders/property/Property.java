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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class Property<T> implements BiConsumer<Property<T>, T> {

    protected T defaultValue;
    protected T value;
    protected final List<BiConsumer<Property<T>, T>> changeListeners;

    public Property(T defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.changeListeners = new ArrayList<>();
    }

    public static <T> Property<T> of(T defaultValue) {
        return new Property<>(defaultValue);
    }

    public void set(T value) {
        this.value = value;
        this.invokeListeners();
    }

    public Property<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public void setToDefault() {
        this.value = this.defaultValue;
        this.invokeListeners();
    }

    public void listen(BiConsumer<Property<T>, T> listener) {
        this.changeListeners.add(listener);
        listener.accept(this, this.value);
    }

    public T get() {
        return value;
    }

    public void copyFrom(Property<T> source) {
        this.defaultValue = source.defaultValue;
        this.value = source.value;
        this.invokeListeners();
    }

    protected void invokeListeners() {
        this.changeListeners.forEach(tConsumer -> tConsumer.accept(this, this.value));
    }

    @Override
    public void accept(Property<T> tProperty, T t) {
        this.set(t);
    }
}
