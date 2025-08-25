package org.sinytra.wiki.exporter.platform.services;

import org.jetbrains.annotations.Nullable;
import org.sinytra.wiki.exporter.ExportContext;

public interface ExporterModuleFactory<T> {
    String name();

    default boolean isEnabled(boolean client) {
        return true;
    }

    @Nullable
    default Class<T> getConfigClass() {
        return null;
    }

    default boolean requiresConfig() {
        return true;
    }

    ExporterModule create(ExportContext context, @Nullable T config);
}
