package org.sinytra.assetexport.dumper;

import net.minecraft.resources.ResourceLocation;

public interface Identifiable {
    // TODO - we're injecting this interface and it could conflict... but it's just in dev so maybe not
    default ResourceLocation getId() {
        return null;
    }
}
