package org.sinytra.wiki.exporter.platform.services;

import org.jetbrains.annotations.Nullable;

public interface ExporterModuleFactory<T> {
    String name();

    default boolean isEnabled(boolean client) {
        return true;
    }

    @Nullable
    default Class<T> getConfigClass() {
        return null;
    }

    ExporterModule create(@Nullable T config);
}
