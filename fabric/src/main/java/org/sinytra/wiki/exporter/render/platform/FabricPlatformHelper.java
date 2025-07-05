package org.sinytra.wiki.exporter.render.platform;

import net.fabricmc.loader.api.FabricLoader;
import org.sinytra.wiki.exporter.platform.services.IPlatformHelper;

import java.nio.file.Path;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public Path getGameDirectory() {
        return FabricLoader.getInstance().getGameDir();
    }
}
