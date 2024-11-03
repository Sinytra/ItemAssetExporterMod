package org.sinytra.assetexport.dumper.impl;

import com.glisco.isometricrenders.mixin.access.SpriteContentsAccessor;
import com.glisco.isometricrenders.render.ItemRenderable;
import com.glisco.isometricrenders.render.RenderableDispatcher;
import com.glisco.isometricrenders.util.ImageIO;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.sinytra.assetexport.dumper.AssetDumper;
import org.sinytra.assetexport.dumper.ObjectSource;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public record ItemAssetDumper(DumpFormat format, int resolution) implements AssetDumper<Item> {
    public static final MapCodec<ItemAssetDumper> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Codec.STRING.fieldOf("format").xmap(x -> DumpFormat.valueOf(x.toUpperCase(Locale.ROOT)), d -> d.name().toLowerCase(Locale.ROOT)).forGetter(ItemAssetDumper::format),
            Codec.INT.fieldOf("resolution").forGetter(ItemAssetDumper::resolution)
    ).apply(in, ItemAssetDumper::new));

    private static final Set<Item> IGNORE_DEPTH = Set.of(Items.SPYGLASS, Items.TRIDENT);

    @Override
    public ObjectSource<Item> getSource() {
        return ObjectSource.fromRegistry(BuiltInRegistries.ITEM);
    }

    @Override
    public void dump(Function<ResourceLocation, Path> file, Item object, Consumer<CompletableFuture<File>> out) {
        ItemStack stack = new ItemStack(object);
        ItemRenderable renderable = new ItemRenderable(stack);

        boolean depth = !IGNORE_DEPTH.contains(object) && Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0).isGui3d();
        setupItem(renderable, depth);

        if (format == DumpFormat.PNG) {
            out.accept(ImageIO.save(
                    RenderableDispatcher.drawIntoImage(renderable, 0, resolution),
                    file.apply(stack.getItem().getId().withSuffix(".png")).toFile()
            ));
        } else {
            out.accept(ImageIO.save(
                    RenderableDispatcher.drawFramed(renderable, renderable.getAnimationTicks(), renderable.getSprites().toList(), resolution),
                    file.apply(stack.getItem().getId().withSuffix(".gif")).toFile()
            ));
        }
    }

    @Override
    public boolean canDump(Item object) {
        return format == DumpFormat.GIF ? ItemRenderable.getModel(object.getDefaultInstance()).getQuads(null, null, RandomSource.create(1L))
                .stream().map(BakedQuad::getSprite).distinct()
                .anyMatch(s -> ((SpriteContentsAccessor) s.contents()).iae$getAnimatedTexture() != null) : true;
    }

    private static void setupItem(ItemRenderable renderable, boolean depth) {
        renderable.properties().slant.set(0);
        renderable.properties().rotation.set(depth ? 272 : 0);
        renderable.properties().lightAngle.set(-45);
        renderable.properties().scale.set(98);
    }

    @Override
    public MapCodec<? extends AssetDumper<Item>> codec() {
        return CODEC;
    }

    public enum DumpFormat {
        PNG,
        GIF
    }
}
