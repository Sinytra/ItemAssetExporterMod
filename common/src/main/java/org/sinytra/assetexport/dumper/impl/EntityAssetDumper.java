package org.sinytra.assetexport.dumper.impl;

import com.glisco.isometricrenders.render.EntityRenderable;
import com.glisco.isometricrenders.render.RenderableDispatcher;
import com.glisco.isometricrenders.util.ImageIO;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.sinytra.assetexport.dumper.AssetDumper;
import org.sinytra.assetexport.dumper.ObjectSource;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class EntityAssetDumper implements AssetDumper<EntityType<?>, Entity> {
    public static final MapCodec<EntityAssetDumper> CODEC = MapCodec.unit(EntityAssetDumper::new);
    @Override
    public ObjectSource<EntityType<?>, Entity> getSource() {
        return ObjectSource.fromRegistry(BuiltInRegistries.ENTITY_TYPE);
    }

    @Override
    public void dump(Function<ResourceLocation, Path> file, Entity object, Consumer<CompletableFuture<File>> out) {
        out.accept(ImageIO.save(
                RenderableDispatcher.drawIntoImage(new EntityRenderable(object, 128), 0, 1024),
                file.apply(object.getType().getId().withSuffix(".png")).toFile()
        ));
    }

    @Override
    public boolean canDump(Entity object) {
        return true;
    }

    @Override
    public MapCodec<? extends AssetDumper<EntityType<?>, Entity>> codec() {
        return CODEC;
    }

    @Override
    public boolean requiresLevel() {
        return true;
    }
}
