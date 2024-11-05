package org.sinytra.assetexport.dumper;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IdentifiableSelector {
    BiMap<String, MapCodec<? extends IdentifiableSelector>> REGISTRY = ImmutableBiMap.of(
            "all", All.CODEC,
            "exclude", Exclude.CODEC,
            "fixed", Fixed.CODEC,
            "with_namespace", WithNamespace.CODEC
    );

    Codec<IdentifiableSelector> CODEC = Codec.STRING.dispatch(
            "type",
            i -> REGISTRY.inverse().get(i.codec()),
            REGISTRY::get
    );

    <Z extends Identifiable<Z>, T extends IdentifiableType<Z>> void select(ObjectSource<T, Z> selector, Map<T, Z> output);

    MapCodec<? extends IdentifiableSelector> codec();

    record All() implements IdentifiableSelector {
        public static final MapCodec<All> CODEC = MapCodec.unit(All::new);

        @Override
        public <Z extends Identifiable<Z>, T extends IdentifiableType<Z>> void select(ObjectSource<T, Z> selector, Map<T, Z> output) {
            for (T t : selector.getAll()) {
                output.put(t, t.defaultIdentifiable());
            }
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
        public <Z extends Identifiable<Z>, T extends IdentifiableType<Z>> void select(ObjectSource<T, Z> selector, Map<T, Z> output) {
            output.keySet().removeIf(k -> ids.contains(k.getId()));
        }

        @Override
        public MapCodec<? extends IdentifiableSelector> codec() {
            return CODEC;
        }
    }

    record Fixed(List<ResourceLocation> ids) implements IdentifiableSelector {
        public static final MapCodec<Fixed> CODEC = ResourceLocation.CODEC
                .listOf().fieldOf("ids").xmap(Fixed::new, Fixed::ids);

        @Override
        public <Z extends Identifiable<Z>, T extends IdentifiableType<Z>> void select(ObjectSource<T, Z> selector, Map<T, Z> output) {
            for (ResourceLocation id : ids) {
                var type = selector.byId(id);
                output.put(type, type.defaultIdentifiable());
            }
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
        public <Z extends Identifiable<Z>, T extends IdentifiableType<Z>> void select(ObjectSource<T, Z> selector, Map<T, Z> output) {
            for (T t : selector.getAll()) {
                if (t.getId().getNamespace().equals(namespace)) {
                    output.put(t, t.defaultIdentifiable());
                }
            }
        }

        @Override
        public MapCodec<? extends IdentifiableSelector> codec() {
            return CODEC;
        }
    }
}
