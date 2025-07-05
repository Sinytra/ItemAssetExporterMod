package org.sinytra.wiki.exporter.render.platform;

import net.neoforged.fml.loading.FMLPaths;
import org.sinytra.wiki.exporter.platform.services.IPlatformHelper;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public Path getGameDirectory() {
        return FMLPaths.GAMEDIR.get();
    }
}