package org.sinytra.assetexport.dumper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record AssetDump<T extends IdentifiableType<Z>, Z extends Identifiable<Z>>(AssetDumper<T, Z> type, List<IdentifiableSelector> selectors, Optional<String> outputLocation) {
    public static final Codec<AssetDump<?, ?>> CODEC = RecordCodecBuilder.create(in -> in.group(
            AssetDumper.CODEC.fieldOf("dumper").forGetter(AssetDump::type),
            IdentifiableSelector.CODEC.listOf().fieldOf("selectors").forGetter(AssetDump::selectors),
            Codec.STRING.optionalFieldOf("location").forGetter(AssetDump::outputLocation)
    ).apply(in, AssetDump::new));
}
