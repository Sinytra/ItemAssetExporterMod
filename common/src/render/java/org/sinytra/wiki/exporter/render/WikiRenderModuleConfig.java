package org.sinytra.wiki.exporter.render;

import org.sinytra.wiki.exporter.NamespacedModuleConfig;

import java.util.Set;

public record WikiRenderModuleConfig(
    Set<String> namespaces,
    int resolution,
    boolean png,
    boolean gif
) implements NamespacedModuleConfig {
}
