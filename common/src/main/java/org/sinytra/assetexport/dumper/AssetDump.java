package org.sinytra.assetexport.dumper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record AssetDump<T extends Identifiable>(AssetDumper<T> type, List<IdentifiableSelector> selectors) {
    public static final Codec<AssetDump<?>> CODEC = RecordCodecBuilder.create(in -> in.group(
            AssetDumper.CODEC.fieldOf("dumper").forGetter(AssetDump::type),
            IdentifiableSelector.CODEC.listOf().fieldOf("selectors").forGetter(AssetDump::selectors)
    ).apply(in, AssetDump::new));
}
