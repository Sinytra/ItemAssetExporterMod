package org.sinytra.wiki.exporter.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageWriter.class);

    public static void writeAsPNG(Path root, String filename, GpuTexture texture, boolean flipY) {
        int i = texture.getFormat().blockSize() * texture.getWidth(0) * texture.getHeight(0);

        GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Texture output buffer", 9, i);
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        Runnable runnable = () -> {
            try (GpuBufferSlice.MappedView mappedView = gpuBuffer.map(true, false)) {
                int width = texture.getWidth(0);
                int height = texture.getHeight(0);

                try (NativeImage nativeimage = new NativeImage(width, height, false)) {
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int invertY = flipY ? height - 1 - y : y;
                            int color = mappedView.data().getInt((x + invertY * width) * texture.getFormat().blockSize());
                            nativeimage.setPixelABGR(x, y, color);
                        }
                    }

                    Path path = root.resolve(filename + ".png");
                    nativeimage.writeToFile(path);
                    LOGGER.debug("Exported png to: {}", path.toAbsolutePath());
                } catch (IOException ioexception) {
                    LOGGER.debug("Unable to write: ", ioexception);
                }
            } catch (Exception exception) {
                LOGGER.error("Unable to write: ", exception);
            }

            gpuBuffer.close();
        };
        AtomicInteger atomicInteger = new AtomicInteger();

        commandEncoder.copyTextureToBuffer(texture, gpuBuffer, 0, () -> {
            if (atomicInteger.getAndIncrement() == 0) {
                runnable.run();
            }
        }, 0);
    }
}
