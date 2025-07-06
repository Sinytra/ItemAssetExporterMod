package org.sinytra.wiki.exporter.render.platform;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.sinytra.wiki.exporter.Constants;
import org.sinytra.wiki.exporter.WikiDataExporter;
import org.sinytra.wiki.exporter.metadata.WikiMetadataModuleFactory;

@Mod(Constants.MOD_ID)
public class WikiExporterMod {

    public WikiExporterMod() {
        WikiDataExporter.initialize();

        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onCommonSetup);
    }
    
    private void onServerStarting(ServerStartingEvent event) {
        WikiDataExporter.setServer(event.getServer());
    }

    private void onCommonSetup(ServerStartedEvent event) {
        WikiDataExporter.runModule(WikiMetadataModuleFactory.NAME);
    }
}
