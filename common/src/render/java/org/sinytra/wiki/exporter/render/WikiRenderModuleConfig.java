package org.sinytra.wiki.exporter.render;

import java.util.Set;

public record WikiRenderModuleConfig(
    Set<String> namespaces,
    int resolution,
    boolean png,
    boolean gif
) {
}
