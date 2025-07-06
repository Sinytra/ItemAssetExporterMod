package org.sinytra.wiki.exporter.platform;

import org.sinytra.wiki.exporter.Constants;
import org.sinytra.wiki.exporter.platform.services.ExporterModuleFactory;
import org.sinytra.wiki.exporter.platform.services.IPlatformHelper;

import java.util.List;
import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final List<ExporterModuleFactory> MODULE_FACTORIES = loadAll(ExporterModuleFactory.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
            .findFirst()
            .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

    public static <T> List<T> loadAll(Class<T> clazz) {
        final List<T> loadedServices = ServiceLoader.load(clazz).stream()
            .map(ServiceLoader.Provider::get)
            .toList();
        Constants.LOG.debug("Loaded {} providers for service {}", loadedServices.size(), clazz);
        return loadedServices;
    }
}