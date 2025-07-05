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
import org.sinytra.wiki.exporter.platform.Services;
import org.sinytra.wiki.exporter.platform.services.ExporterModule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WikiExporterRenderer implements ExporterModule {
    public static final ResourceLocation NAME = Constants.location("render");

    public static final String RENDER_PROPERTY = "wiki_exporter.render.namespaces";
    public static final String OUTPUT_PROPERTY = "wiki_exporter.render.output";
    public static final boolean PNGS = getBoolean("wiki_exporter.render.outputs.png", true);
    //    public static final boolean ANIMATED_GIFS = getBoolean("wiki_exporter.render.outputs.gif", false);
    private static final int RESOLUTION = 128;

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public boolean isEnabled() {
        return shouldRender();
    }

    @Override
    public void run() {
        List<Pair<ResourceLocation, Item>> renderable = getRenderableItems();
        if (!renderable.isEmpty()) {
            String outputProperty = System.getProperty(OUTPUT_PROPERTY);
            Path path = outputProperty != null ? Path.of(outputProperty) : Services.PLATFORM.getGameDirectory();

            renderable.stream()
                .map(p -> p.getFirst().getNamespace())
                .forEach(n -> {
                    try {
                        Files.createDirectories(path.resolve(n));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

            Constants.LOG.info("Rendering {} items", renderable.size());
            renderItems(renderable, path).join();
        }
    }

    public static boolean shouldRender() {
        return System.getProperty(RENDER_PROPERTY) != null;
    }

    private static List<Pair<ResourceLocation, Item>> getRenderableItems() {
        Set<String> namespaces = Set.of(System.getProperty(RENDER_PROPERTY).split(","));
        if (namespaces.isEmpty()) {
            return List.of();
        }

        Constants.LOG.info("Rendering items for namespaces {}", namespaces);

        List<Pair<ResourceLocation, Item>> list = new ArrayList<>();
        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            ResourceLocation name = entry.getKey().location();
            if (namespaces.contains(name.getNamespace())) {
                list.add(Pair.of(name, entry.getValue()));
            }
        }

        return list;
    }

    private static CompletableFuture<?> renderItems(List<Pair<ResourceLocation, Item>> renderable, Path root) {
        RenderTarget target = new TextureTarget("Wiki Exporter", RESOLUTION, RESOLUTION, true);
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        SimpleItemRenderer renderer = new SimpleItemRenderer(target, bufferSource, 32);

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

    private static void scheduleRender(ItemStack stack, RenderTarget target, SimpleItemRenderer renderer, Path output) {
        if (PNGS) {
            exportRenderItem(output, target, renderer, stack);
        }

//        if (ANIMATED_GIFS) {
//            var frames = renderable.getAnimationTicks();
//            if (frames > 1) {
//                var other = ImageIO.save(
//                    RenderableDispatcher.drawFramed(renderable, frames, renderable.getSprites().toList(), RESOLUTION),
//                    output.resolve(namespace + "/" + fileName + ".gif").toFile()
//                );
//                if (cf != null) {
//                    cf = CompletableFuture.allOf(cf, other);
//                } else {
//                    cf = other;
//                }
//            }
//        }
    }

    private static void exportRenderItem(Path root, RenderTarget target, SimpleItemRenderer renderer, ItemStack stack) {
        ResourceLocation name = stack.getItem().builtInRegistryHolder().key().location();

        RenderSystem.getDevice()
            .createCommandEncoder()
            .clearColorAndDepthTextures(target.getColorTexture(), 0, target.getDepthTexture(), 1.0);

        renderer.renderItem(stack);

        ImageWriter.writeAsPNG(root, name.getPath(), target.getColorTexture(), true);
    }

    private static boolean getBoolean(String name, boolean def) {
        return Boolean.parseBoolean(System.getProperty(name, String.valueOf(def)));
    }
}
