package org.sinytra.wiki.exporter.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
        List<Pair<Identifier, Item>> renderable = getRenderableItems();
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

    private List<Pair<Identifier, Item>> getRenderableItems() {
        if (this.namespaces.isEmpty()) {
            return List.of();
        }

        Constants.LOG.info("Rendering items for namespaces {}", this.namespaces);

        List<Pair<Identifier, Item>> list = new ArrayList<>();
        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            Identifier name = entry.getKey().identifier();
            if (this.namespaces.contains(name.getNamespace())) {
                list.add(Pair.of(name, entry.getValue()));
            }
        }

        return list;
    }

    private CompletableFuture<?> renderItems(List<Pair<Identifier, Item>> renderable, Path root) {
        int resolution = this.config.resolution();
        RenderTarget target = new TextureTarget("Wiki Exporter", resolution, resolution, true, GpuFormat.RGBA8_UNORM);
        SimpleItemRenderer renderer = new SimpleItemRenderer(target, 32);

        List<CompletableFuture<?>> list = renderable.stream()
            .<CompletableFuture<?>>map(p -> {
                Identifier location = p.getFirst();
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

//        if (this.config.gif()) {
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
        Identifier name = stack.getItem().builtInRegistryHolder().key().identifier();

        RenderSystem.getDevice()
            .createCommandEncoder()
            .clearColorAndDepthTextures(target.getColorTexture(), GuiRenderer.CLEAR_COLOR, target.getDepthTexture(), 0.0);

        renderer.renderItem(stack);

        ImageWriter.writeAsPNG(root, name.getPath(), target.getColorTexture(), true);
    }
}
