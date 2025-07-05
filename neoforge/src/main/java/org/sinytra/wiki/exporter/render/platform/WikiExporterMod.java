package org.sinytra.wiki.exporter.render.platform;

import net.neoforged.fml.common.Mod;
import org.sinytra.wiki.exporter.Constants;
import org.sinytra.wiki.exporter.platform.ExporterModules;

@Mod(Constants.MOD_ID)
public class WikiExporterMod {

    public WikiExporterMod() {
        ExporterModules.initialize();
    }
}
