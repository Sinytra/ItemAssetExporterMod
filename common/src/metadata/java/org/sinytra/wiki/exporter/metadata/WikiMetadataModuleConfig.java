package org.sinytra.wiki.exporter.metadata;

import java.util.Set;

public record WikiMetadataModuleConfig(
    Set<String> namespaces
) {}
