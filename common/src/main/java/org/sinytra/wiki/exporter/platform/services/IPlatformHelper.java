package org.sinytra.wiki.exporter.platform.services;

import java.nio.file.Path;

public interface IPlatformHelper {
    Path getGameDirectory();

    boolean isClient();
}