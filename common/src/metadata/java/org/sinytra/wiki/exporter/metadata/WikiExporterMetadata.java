package org.sinytra.wiki.exporter.metadata;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import org.sinytra.wiki.exporter.Constants;
import org.sinytra.wiki.exporter.platform.Services;
import org.sinytra.wiki.exporter.platform.services.ExporterModule;

import java.nio.file.Files;
import java.nio.file.Path;

public class WikiExporterMetadata implements ExporterModule {
    public static final ResourceLocation NAME = Constants.location("metadata");

    private static final String EXPORT_PROPERTY = "wiki_exporter.metadata.namespaces";
    private static final String OUTPUT_PROPERTY = "wiki_exporter.metadata.output";

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void run() {
        String outputProperty = System.getProperty(OUTPUT_PROPERTY);
        Path path = outputProperty != null ? Path.of(outputProperty) : Services.PLATFORM.getGameDirectory().resolve("wiki_exporter");
        try {
            Files.createDirectories(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (Block block : BuiltInRegistries.BLOCK) {
            getBlockMetadata(block);
        }
    }

    record BlockMetadata(
        int stackSize,
        ItemStack requiredTool,
        double blastResistance,
        double hardness,
        boolean flammable,
        MapColor mapColor
    ) {
    }

    private static BlockMetadata getBlockMetadata(Block block) {
        ItemStack effectiveTool = ToolTierDictionary.getEffectiveTool(block.defaultBlockState());
        return null;
    }
}
