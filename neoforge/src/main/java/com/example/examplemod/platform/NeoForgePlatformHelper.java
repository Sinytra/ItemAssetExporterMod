package com.example.examplemod.platform;

import com.example.examplemod.platform.services.IPlatformHelper;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public Path getGameDirectory() {
        return FMLPaths.GAMEDIR.get();
    }
}