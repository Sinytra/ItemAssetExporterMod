package org.sinytra.wiki.exporter.metadata;

import org.sinytra.wiki.exporter.NamespacedModuleConfig;

import java.util.Set;

public record WikiMetadataModuleConfig(
    Set<String> namespaces
) implements NamespacedModuleConfig {}
