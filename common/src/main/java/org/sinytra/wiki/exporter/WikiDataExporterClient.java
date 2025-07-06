package org.sinytra.wiki.exporter;

import net.minecraft.client.Minecraft;

public class WikiDataExporterClient {
    public static void finishRunning() {
        Minecraft.getInstance().stop();
    }
}
