package org.sinytra.wiki.exporter;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExporterConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder()
        .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();

    public record MainConfig(
        @Nullable String outputPath,
        @Nullable Set<String> enabled,
        @Nullable Map<String, JsonElement> modules
    ) {}

    public record LoadedConfig(
        MainConfig global,
        Map<String, Object> modules
    ) {
        @SuppressWarnings("unchecked")
        @Nullable
        public <T> T getForModule(String name) {
            return (T) modules.get(name);
        }
    }

    public static MainConfig preloadConfig(Path path) {
        JsonObject root;
        try (Reader reader = Files.newBufferedReader(path)) {
            root = GSON.fromJson(reader, JsonElement.class).getAsJsonObject();
        } catch (Exception e) {
            LOGGER.error("Error reading exporter config", e);
            return new MainConfig(null, Set.of(), Map.of());
        }

        MainConfig parsed = GSON.fromJson(root, MainConfig.class);
        return new MainConfig(
            parsed.outputPath(),
            parsed.enabled() != null ? parsed.enabled() : Set.of(),
            parsed.modules() != null ? parsed.modules() : Map.of()
        );
    }

    public static LoadedConfig load(MainConfig mainConfig, Map<String, Class<?>> modules) {
        Map<String, Object> moduleConfig = new HashMap<>();

        if (mainConfig.modules() != null) {
            modules.forEach((k, v) -> {
                if (v != null && mainConfig.modules().containsKey(k)) {
                    try {
                        JsonElement moduleRoot = mainConfig.modules().get(k);
                        Object instance = GSON.fromJson(moduleRoot, v);
                        moduleConfig.put(k, instance);
                    } catch (Exception e) {
                        LOGGER.error("Error reading exporter module config for {}", k, e);
                    }
                }
            });
        }

        return new LoadedConfig(mainConfig, moduleConfig);
    }
}
