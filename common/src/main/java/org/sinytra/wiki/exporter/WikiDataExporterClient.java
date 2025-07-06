package org.sinytra.wiki.exporter;

import net.minecraft.client.Minecraft;

public class WikiDataExporterClient {
    public static void finishRunning() {
        if (Minecraft.getInstance() != null) {
            Minecraft.getInstance().stop();
        } else {
            System.exit(0);
        }
    }
}
