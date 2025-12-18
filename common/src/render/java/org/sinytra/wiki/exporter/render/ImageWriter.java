package org.sinytra.wiki.exporter.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ImageWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageWriter.class);

    public static void writeAsPNG(Path root, String filename, RenderTarget target, boolean flipY) {
        int width = target.width;
        int height = target.height;
        NativeImage image = new NativeImage(width, height, false);
        RenderSystem.bindTexture(target.getColorTextureId());
        image.downloadTexture(0, false);
        if (flipY) {
            image.flipY();
        }

        Util.ioPool().execute(() -> {
            try {
                Path path = root.resolve(filename + ".png");
                image.writeToFile(path);
                LOGGER.debug("Exported png to: {}", path.toAbsolutePath());
            } catch (Exception exception) {
                LOGGER.error("Unable to write: ", exception);
            }
        });
    }
}
