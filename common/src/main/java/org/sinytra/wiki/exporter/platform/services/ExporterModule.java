package org.sinytra.wiki.exporter.platform.services;

import net.minecraft.resources.ResourceLocation;

public interface ExporterModule {
    ResourceLocation getName();

    boolean isEnabled();

    void run();
}
