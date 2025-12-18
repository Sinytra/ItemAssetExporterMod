package org.sinytra.wiki.exporter.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.sinytra.wiki.exporter.Constants;
import org.sinytra.wiki.exporter.platform.services.ExporterModule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WikiExporterRenderer implements ExporterModule {
    private final Set<String> namespaces;
    private final WikiRenderModuleConfig config;

    public WikiExporterRenderer(Set<String> namespaces, WikiRenderModuleConfig config) {
        this.namespaces = namespaces;
        this.config = config;
    }

    @Override
    public void run(Path output) {
        List<Pair<ResourceLocation, Item>> renderable = getRenderableItems();
        if (!renderable.isEmpty()) {

            renderable.stream()
                .map(p -> p.getFirst().getNamespace())
                .forEach(n -> {
                    try {
                        Files.createDirectories(output.resolve(n));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

            Constants.LOG.info("Rendering {} items", renderable.size());
            renderItems(renderable, output).join();
        }
    }

    private List<Pair<ResourceLocation, Item>> getRenderableItems() {
        if (this.namespaces.isEmpty()) {
            return List.of();
        }

        Constants.LOG.info("Rendering items for namespaces {}", this.namespaces);

        List<Pair<ResourceLocation, Item>> list = new ArrayList<>();
        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            ResourceLocation name = entry.getKey().location();
            if (this.namespaces.contains(name.getNamespace())) {
                list.add(Pair.of(name, entry.getValue()));
            }
        }

        return list;
    }

    private CompletableFuture<?> renderItems(List<Pair<ResourceLocation, Item>> renderable, Path root) {
        int resolution = this.config.resolution();
        RenderTarget target = new TextureTarget(resolution, resolution, true, Minecraft.ON_OSX);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        SimpleItemRenderer renderer = new SimpleItemRenderer(bufferSource, 32);

        List<CompletableFuture<?>> list = renderable.stream()
            .<CompletableFuture<?>>map(p -> {
                ResourceLocation location = p.getFirst();
                ItemStack stack = new ItemStack(p.getSecond());
                Path output = root.resolve(location.getNamespace());

                return Minecraft.getInstance().submit(() -> scheduleRender(stack, target, renderer, output));
            })
            .toList();

        return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
    }

    private void scheduleRender(ItemStack stack, RenderTarget target, SimpleItemRenderer renderer, Path output) {
        if (this.config.png()) {
            exportRenderItem(output, target, renderer, stack);
        }
    }

    private static void exportRenderItem(Path root, RenderTarget target, SimpleItemRenderer renderer, ItemStack stack) {
        ResourceLocation name = stack.getItem().builtInRegistryHolder().key().location();

        RenderSystem.enableBlend();
        RenderSystem.clear(16640, Minecraft.ON_OSX);

        target.setClearColor(0, 0, 0, 0);
        target.clear(Minecraft.ON_OSX);

        target.bindWrite(true);
        renderer.renderItem(stack);
        target.unbindWrite();

        ImageWriter.writeAsPNG(root, name.getPath(), target, true);
    }
}
