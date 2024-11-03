package org.sinytra.assetexport;

import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.sinytra.assetexport.dumper.AssetDump;
import org.sinytra.assetexport.dumper.Identifiable;
import org.sinytra.assetexport.dumper.IdentifiableSelector;
import org.sinytra.assetexport.platform.Services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommonClass {
    public static final String CONFIG_FILE = "item_asset_export.render.config.file";
    public static final String OUTPUT_PROPERTY = "item_asset_export.render.output";

    public static void runRender() {
        if (shouldRender()) {
            try {
                String outputProperty = System.getProperty(OUTPUT_PROPERTY);
                Path path = outputProperty != null ? Path.of(outputProperty) : Services.PLATFORM.getGameDirectory();

                var dumps = getDumps();
                for (int i = 0; i < dumps.size(); i++) {
                    var dump = dumps.get(i);
                    Constants.LOG.info("Running dump {} ({} selecting {})", i, dump, dump.selectors().stream()
                            .map(Object::toString).collect(Collectors.joining(", ")));

                    runDump(path, dump);

                    Constants.LOG.info("Finished dump {}", i);
                }

                Constants.LOG.info("Render complete, shutting down");
                Minecraft.getInstance().stop();
            } catch (IOException e) {
                throw new RuntimeException("Failed to read dumps file", e);
            }
        }
    }

    public static boolean shouldRender() {
        return System.getProperty(CONFIG_FILE) != null;
    }

    private static List<AssetDump<?>> getDumps() throws IOException {
        var file = Path.of(System.getProperty(CONFIG_FILE));
        var dumps = AssetDump.CODEC.listOf()
                .decode(JsonOps.INSTANCE, GsonHelper.parseArray(Files.readString(file)))
                .getOrThrow();
        return dumps.getFirst();
    }

    private static <T extends Identifiable> void runDump(Path basePath, AssetDump<T> dump) {
        var selected = new HashSet<T>();
        var universe = dump.type().getSource();
        for (IdentifiableSelector selector : dump.selectors()) {
            selector.select(universe, selected);
        }

        selected.removeIf(Predicate.not(dump.type()::canDump));
        if (selected.isEmpty()) return;

        Function<ResourceLocation, Path> pathFunction = location -> basePath.resolve(location.getNamespace() + "/" + location.getPath());

        Constants.LOG.info("Dumping {} objects...", selected.size());

        var output = Collections.synchronizedList(new ArrayList<CompletableFuture<File>>());
        CompletableFuture.allOf(selected.stream().map(s -> Minecraft.getInstance().submit(() ->
                        dump.type().dump(pathFunction, s, output::add)))
                .toArray(CompletableFuture[]::new)).join();

        CompletableFuture.allOf(output.toArray(CompletableFuture[]::new)).join();
    }

}
