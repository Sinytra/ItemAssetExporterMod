package org.sinytra.wiki.exporter.platform;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.sinytra.wiki.exporter.platform.services.ExporterModule;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ExporterModules {
    private static final Map<ResourceLocation, ExporterModule> MODULES = new HashMap<>();
    private static final Map<ResourceLocation, CompletableFuture<Void>> FUTURES = new HashMap<>();
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void initialize() {
        LOGGER.info("Initializing Wiki Exporter");

        Services.MODULES.stream()
            .filter(ExporterModule::isEnabled)
            .forEach(module -> MODULES.put(module.getName(), module));

        LOGGER.info("Found {} enabled modules", MODULES.size());

        MODULES.forEach((name, module) -> FUTURES.put(
            name,
            new CompletableFuture<>()
        ));

        CompletableFuture.allOf(FUTURES.values().toArray(new CompletableFuture[0]))
            .thenRunAsync(() -> {
                LOGGER.info("Finished running all modules, shutting down");

                Minecraft.getInstance().stop();
            });
    }

    public static void runModule(ResourceLocation location) {
        Optional.ofNullable(MODULES.get(location))
            .ifPresent(module -> {
                module.run();
                CompletableFuture<Void> future = FUTURES.get(location);
                if (future != null) {
                    future.complete(null);
                    LOGGER.info("Finished running module {}", module.getName());
                }
            });
    }
}
