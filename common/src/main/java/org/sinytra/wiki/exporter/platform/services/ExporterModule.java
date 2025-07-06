package org.sinytra.wiki.exporter.platform.services;

import java.nio.file.Path;

public interface ExporterModule {
    void run(Path output) throws Exception;
}
