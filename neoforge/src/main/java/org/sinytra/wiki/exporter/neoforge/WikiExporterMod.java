package org.sinytra.wiki.exporter.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
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

        if (FMLEnvironment.getDist() == Dist.DEDICATED_SERVER) {
            NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        }
    }

    private void onServerStarting(ServerStartingEvent event) {
        WikiDataExporter.setServer(event.getServer());
    }

    private void onServerStarted(ServerStartedEvent event) {
        WikiDataExporter.runModule(WikiMetadataModuleFactory.NAME);
    }
}
