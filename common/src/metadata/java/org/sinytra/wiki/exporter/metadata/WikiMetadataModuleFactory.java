package org.sinytra.wiki.exporter.metadata;

import org.jetbrains.annotations.Nullable;
import org.sinytra.wiki.exporter.platform.services.ExporterModule;
import org.sinytra.wiki.exporter.platform.services.ExporterModuleFactory;

public class WikiMetadataModuleFactory implements ExporterModuleFactory<WikiMetadataModuleConfig> {
    public static final String NAME = "metadata";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean isEnabled(boolean client) {
        return !client;
    }

    @Nullable
    @Override
    public Class<WikiMetadataModuleConfig> getConfigClass() {
        return WikiMetadataModuleConfig.class;
    }

    @Override
    public ExporterModule create(WikiMetadataModuleConfig config) {
        return new WikiExporterMetadata(config);
    }
}
