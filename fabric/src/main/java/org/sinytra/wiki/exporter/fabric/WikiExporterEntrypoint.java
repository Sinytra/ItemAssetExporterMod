package org.sinytra.wiki.exporter.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.sinytra.wiki.exporter.WikiDataExporter;
import org.sinytra.wiki.exporter.metadata.WikiMetadataModuleFactory;

public class WikiExporterEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        WikiDataExporter.initialize();

        ServerLifecycleEvents.SERVER_STARTING.register(WikiDataExporter::setServer);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> WikiDataExporter.runModule(WikiMetadataModuleFactory.NAME));
    }
}
