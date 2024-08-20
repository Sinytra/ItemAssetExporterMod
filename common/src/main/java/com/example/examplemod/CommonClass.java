package com.example.examplemod;

import com.example.examplemod.platform.Services;
import com.glisco.isometricrenders.util.ImageIO;
import com.glisco.isometricrenders.render.ItemRenderable;
import com.glisco.isometricrenders.render.RenderableDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CommonClass {
    public static final String RENDER_PROPERTY = "item_asset_export.render.namespaces";
    private static final int RESOLUTION = 128;
    private static final Set<Item> IGNORE_DEPTH = Set.of(Items.SPYGLASS, Items.TRIDENT);

    public static void runRender() {
        if (shouldRender()) {
            renderItems().join();
            Constants.LOG.info("Render complete, shutting down");
            Minecraft.getInstance().stop();
        }
    }

    private static boolean shouldRender() {
        return System.getProperty(RENDER_PROPERTY) != null;
    }

    private static CompletableFuture<?> renderItems() {
        Set<String> namespaces = Set.of(System.getProperty(RENDER_PROPERTY).split(","));
        if (namespaces.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        Constants.LOG.info("Rendering items for namespaces {}", namespaces);

        List<CompletableFuture<?>> list = new ArrayList<>();
        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            ResourceLocation name = entry.getKey().location();
            if (namespaces.contains(name.getNamespace())) {
                list.add(renderItem(name, entry.getValue()));
            }
        }

        return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
    }

    private static CompletableFuture<?> renderItem(ResourceLocation name, Item item) {
        return Minecraft.getInstance().submit(() -> {
            ItemStack stack = new ItemStack(item);
            ItemRenderable renderable = new ItemRenderable(stack);

            boolean depth = !IGNORE_DEPTH.contains(item) && Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0).isGui3d();
            setupItem(renderable, depth);

            return scheduleRender(renderable, name.getNamespace(), name.getPath());
        }).thenCompose(Function.identity());
    }

    private static CompletableFuture<File> scheduleRender(ItemRenderable renderable, String namespace, String fileName) {
        Path gameDir = Services.PLATFORM.getGameDirectory();
        return ImageIO.save(
            RenderableDispatcher.drawIntoImage(renderable, 0, RESOLUTION),
            gameDir.resolve("renders/" + namespace + "/" + fileName + ".png").toFile()
        );
    }

    private static void setupItem(ItemRenderable renderable, boolean depth) {
        renderable.properties().slant.set(0);
        renderable.properties().rotation.set(depth ? 272 : 0);
        renderable.properties().lightAngle.set(-45);
        renderable.properties().scale.set(98);
    }
}