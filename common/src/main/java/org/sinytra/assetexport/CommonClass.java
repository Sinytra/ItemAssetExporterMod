package org.sinytra.assetexport;

import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.sinytra.assetexport.dumper.AssetDump;
import org.sinytra.assetexport.dumper.Identifiable;
import org.sinytra.assetexport.dumper.IdentifiableSelector;
import org.sinytra.assetexport.dumper.IdentifiableType;
import org.sinytra.assetexport.platform.Services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommonClass {
    public static final String CONFIG_FILE = "item_asset_export.render.config.file";
    public static final String OUTPUT_PROPERTY = "item_asset_export.render.output";
    public static boolean render, loadWorld;
    public static Runnable mainThread;
    public static Thread mainTh;
    public static ReentrantBlockableEventLoop<Runnable> renderQueue;

    public static void startRender() {
        if (shouldRender()) {
            render = true;

            try {
                loadWorld = getDumps().stream().anyMatch(a -> a.type().requiresLevel());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void queueTasks(ReentrantBlockableEventLoop<Runnable> thread) {
        try {
            String outputProperty = System.getProperty(OUTPUT_PROPERTY);
            Path path = outputProperty != null ? Path.of(outputProperty) : Services.PLATFORM.getGameDirectory();

            var dumps = getDumps();
            for (int i = 0; i < dumps.size(); i++) {
                var dump = dumps.get(i);
                Constants.LOG.info("Running dump {} ({} selecting {})", i, dump, dump.selectors().stream()
                        .map(Object::toString).collect(Collectors.joining(", ")));

                runDump(dump.outputLocation().map(path::resolve).orElse(path), (AssetDump) dump, thread);

                Constants.LOG.info("Finished dump {}", i);
            }

            Constants.LOG.info("Render complete in {}ms, shutting down", System.currentTimeMillis() - ProgressTracker.start);
            if (Minecraft.getInstance().hasSingleplayerServer()) {
                Minecraft.getInstance().getSingleplayerServer().halt(true);
            }
            Minecraft.getInstance().stop();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read dumps file", e);
        }
    }

    public static boolean shouldRender() {
        return System.getProperty(CONFIG_FILE) != null;
    }

    private static List<AssetDump<?, ?>> getDumps() throws IOException {
        var file = Path.of(System.getProperty(CONFIG_FILE));
        var dumps = AssetDump.CODEC.listOf()
                .decode(JsonOps.INSTANCE, GsonHelper.parseArray(Files.readString(file)))
                .getOrThrow();
        return dumps.getFirst();
    }

    private static <Z extends Identifiable<Z>, T extends IdentifiableType<Z>> void runDump(Path basePath, AssetDump<T, Z> dump, ReentrantBlockableEventLoop<Runnable> thread) {
        var selected = new HashMap<T, Z>();
        var universe = dump.type().getSource();
        for (IdentifiableSelector selector : dump.selectors()) {
            selector.select(universe, selected);
        }

        selected.values().removeIf(z -> z == null || !dump.type().canDump(z));
        if (selected.isEmpty()) return;

        Function<ResourceLocation, Path> pathFunction = location -> basePath.resolve(location.getNamespace() + "/" + location.getPath());

        ProgressTracker.generated = new ProgressTracker.Counter(selected.size());
        ProgressTracker.dumped = new ProgressTracker.Counter(selected.size());
        ProgressTracker.start = System.currentTimeMillis();

        Constants.LOG.info("Dumping {} objects...", selected.size());

        var output = Collections.synchronizedList(new ArrayList<CompletableFuture<File>>());
        CompletableFuture.allOf(selected.values().stream().map(s -> thread.submit(() ->
                {
                    ProgressTracker.currentRender = s;
                    dump.type().dump(pathFunction, s, fu -> output.add(fu.thenApply(f -> {
                        ProgressTracker.dumped.increment();
                        return f;
                    })));
                    ProgressTracker.generated.increment();
                }))
                .toArray(CompletableFuture[]::new)).join();

        CompletableFuture.allOf(output.toArray(CompletableFuture[]::new)).join();

        ProgressTracker.currentRender = null;
    }

}
