package org.sinytra.assetexport.dumper;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import org.sinytra.assetexport.dumper.impl.ItemAssetDumper;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface AssetDumper<T extends Identifiable> {
    BiMap<String, MapCodec<? extends AssetDumper<?>>> REGISTRY = ImmutableBiMap.of(
            "item", ItemAssetDumper.CODEC
    );

    Codec<AssetDumper<?>> CODEC = Codec.STRING.dispatch(
            "type",
            d -> REGISTRY.inverse().get(d.codec()),
            REGISTRY::get
    );

    ObjectSource<T> getSource();

    void dump(Function<ResourceLocation, Path> file, T object, Consumer<CompletableFuture<File>> out);

    boolean canDump(T object);

    MapCodec<? extends AssetDumper<T>> codec();
}
