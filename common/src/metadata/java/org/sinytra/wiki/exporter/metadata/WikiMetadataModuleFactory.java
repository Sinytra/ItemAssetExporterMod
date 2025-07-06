package org.sinytra.wiki.exporter.metadata;

import org.sinytra.wiki.exporter.platform.services.ExporterModule;
import org.sinytra.wiki.exporter.platform.services.ExporterModuleFactory;

public class WikiMetadataModuleFactory implements ExporterModuleFactory<Void> {
    public static final String NAME = "metadata";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public ExporterModule create(Void config) {
        return new WikiExporterMetadata();
    }
}
