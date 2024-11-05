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
package com.glisco.isometricrenders.util;

import com.mojang.blaze3d.platform.NativeImage;
import org.sinytra.assetexport.Constants;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageIO {

    private static final AtomicInteger TASK_COUNT = new AtomicInteger(0);

    public static CompletableFuture<File> save(List<NativeImage> frames, File imageFile) {
        return save(out -> {
            try (var writer = new GifSequenceWriter(new BufferedOutputStream(new FileOutputStream(out)), BufferedImage.TYPE_INT_ARGB, 1000/20, true)) {
                for (NativeImage frame : frames) {
                    writer.writeToSequence(frame);
                }
            }
        }, imageFile);
    }

    public static CompletableFuture<File> save(NativeImage image, File imageFile) {
        return save(out -> {
            image.writeToFile(out);
            image.close();
        }, imageFile);
    }

    public static CompletableFuture<File> save(IoWriter image, File imageFile) {
        final var future = new CompletableFuture<File>();

        TASK_COUNT.incrementAndGet();
        ForkJoinPool.commonPool().submit(() -> {
            imageFile.getParentFile().mkdirs();

            try {
                image.write(imageFile);
                Constants.LOG.info("Image " + imageFile.getAbsolutePath() + " saved");
                future.complete(imageFile);
            } catch (IOException e) {
                Constants.LOG.warn("Could not save image " + imageFile.getAbsolutePath(), e);
                future.completeExceptionally(e);
            } finally {
                TASK_COUNT.decrementAndGet();
            }
        });

        return future;
    }

    @FunctionalInterface
    public interface IoWriter {
        void write(File out) throws IOException;
    }
}
