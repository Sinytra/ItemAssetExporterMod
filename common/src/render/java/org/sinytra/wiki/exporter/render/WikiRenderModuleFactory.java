package org.sinytra.wiki.exporter.render;

import org.jetbrains.annotations.Nullable;
import org.sinytra.wiki.exporter.platform.services.ExporterModule;
import org.sinytra.wiki.exporter.platform.services.ExporterModuleFactory;

public class WikiRenderModuleFactory implements ExporterModuleFactory<WikiRenderModuleConfig> {
    public static final String NAME = "render";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean isEnabled(boolean client) {
        return client;
    }

    @Nullable
    @Override
    public Class<WikiRenderModuleConfig> getConfigClass() {
        return WikiRenderModuleConfig.class;
    }

    @Override
    public ExporterModule create(WikiRenderModuleConfig config) {
        return new WikiExporterRenderer(config);
    }
}
