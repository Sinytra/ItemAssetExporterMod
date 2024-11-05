package org.sinytra.assetexport.dumper;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public interface ObjectSource<T extends IdentifiableType<Z>, Z extends Identifiable<Z>> {
    T byId(ResourceLocation id);

    Collection<T> getAll();

    static <Z extends Identifiable<Z>, T extends IdentifiableType<Z>> ObjectSource<T, Z> fromRegistry(Registry<T> reg) {
        return new ObjectSource<>() {
            @Override
            public T byId(ResourceLocation id) {
                return reg.get(id);
            }

            @Override
            public Collection<T> getAll() {
                return reg.stream().toList();
            }
        };
    }
}
