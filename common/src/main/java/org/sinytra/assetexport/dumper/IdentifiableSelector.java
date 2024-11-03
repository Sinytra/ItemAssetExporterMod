package org.sinytra.assetexport.dumper;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public interface IdentifiableSelector {
    BiMap<String, MapCodec<? extends IdentifiableSelector>> REGISTRY = ImmutableBiMap.of(
            "all", All.CODEC,
            "exclude", Exclude.CODEC,
            "with_namespace", WithNamespace.CODEC
    );

    Codec<IdentifiableSelector> CODEC = Codec.STRING.dispatch(
            "type",
            i -> REGISTRY.inverse().get(i.codec()),
            REGISTRY::get
    );

    <T extends Identifiable> void select(ObjectSource<T> selector, Set<T> output);

    MapCodec<? extends IdentifiableSelector> codec();

    record All() implements IdentifiableSelector {
        public static final MapCodec<All> CODEC = MapCodec.unit(All::new);

        @Override
        public <T extends Identifiable> void select(ObjectSource<T> selector, Set<T> output) {
            output.addAll(selector.getAll());
        }

        @Override
        public MapCodec<? extends IdentifiableSelector> codec() {
            return CODEC;
        }
    }

    record Exclude(Set<ResourceLocation> ids) implements IdentifiableSelector {
        public static final MapCodec<Exclude> CODEC = ResourceLocation.CODEC
                .listOf().fieldOf("ids").xmap(l -> new Exclude(new HashSet<>(l)), exclude -> new ArrayList<>(exclude.ids()));

        @Override
        public <T extends Identifiable> void select(ObjectSource<T> selector, Set<T> output) {
            output.removeIf(o -> ids.contains(o.getId()));
        }

        @Override
        public MapCodec<? extends IdentifiableSelector> codec() {
            return CODEC;
        }
    }

    record WithNamespace(String namespace) implements IdentifiableSelector {
        public static final MapCodec<WithNamespace> CODEC = Codec.STRING
                .fieldOf("namespace").xmap(WithNamespace::new, WithNamespace::namespace);

        @Override
        public <T extends Identifiable> void select(ObjectSource<T> selector, Set<T> output) {
            for (T t : selector.getAll()) {
                if (t.getId().getNamespace().equals(namespace)) output.add(t);
            }
        }

        @Override
        public MapCodec<? extends IdentifiableSelector> codec() {
            return CODEC;
        }
    }
}
