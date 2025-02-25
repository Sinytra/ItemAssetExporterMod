package org.sinytra.assetexport.dumper;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import org.sinytra.assetexport.dumper.impl.EntityAssetDumper;
import org.sinytra.assetexport.dumper.impl.ItemAssetDumper;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface AssetDumper<T extends IdentifiableType<Z>, Z extends Identifiable<Z>> {
    BiMap<String, MapCodec<? extends AssetDumper<?, ?>>> REGISTRY = ImmutableBiMap.of(
            "item", ItemAssetDumper.CODEC,
            "entity", EntityAssetDumper.CODEC
    );

    Codec<AssetDumper<?, ?>> CODEC = Codec.STRING.dispatch(
            "type",
            d -> REGISTRY.inverse().get(d.codec()),
            REGISTRY::get
    );

    ObjectSource<T, Z> getSource();

    void dump(Function<ResourceLocation, Path> file, Z object, Consumer<CompletableFuture<File>> out);

    boolean canDump(Z object);

    MapCodec<? extends AssetDumper<T, Z>> codec();

    default boolean requiresLevel() {
        return false;
    }
}
