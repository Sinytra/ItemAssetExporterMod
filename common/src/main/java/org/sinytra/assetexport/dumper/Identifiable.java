package org.sinytra.assetexport.dumper;

public interface Identifiable<T extends Identifiable<T>> {
    default IdentifiableType<T> getIdentifiableType() {
        return null;
    }

    default String representAsString() {
        return null;
    }
}
