package org.sinytra.wiki.exporter.metadata;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.sinytra.wiki.exporter.platform.services.ExporterModule;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class WikiExporterMetadata implements ExporterModule {
    private final WikiMetadataModuleConfig config;

    public WikiExporterMetadata(WikiMetadataModuleConfig config) {
        this.config = config;
    }

    @Override
    public void run(Path output) throws Exception {
        if (this.config.namespaces() == null) {
            return;
        }

        Map<String, BlockMetadata> metadata = new HashMap<>();
        for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
            ResourceLocation name = entry.getKey().location();
            Block block = entry.getValue();
            if (!this.config.namespaces().contains(name.getNamespace())) {
                continue;
            }

            BlockMetadata data = getBlockMetadata(block);
            if (data == null) {
                continue;
            }
            metadata.put(name.toString(), data);
        }

        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

        String content = gson.toJson(metadata);

        Files.writeString(output.resolve("properties.json"), content, StandardCharsets.UTF_8);
    }

    record BlockMetadata(
        int stackSize,
        String requiredTool,
        float blastResistance,
        Float hardness,
        boolean flammable
    ) {
    }

    private static BlockMetadata getBlockMetadata(Block block) {
        BlockState state = block.defaultBlockState();
        ItemStack effectiveTool = ToolTierDictionary.getEffectiveTool(state);
        String effectiveToolId = effectiveTool.isEmpty() ? null : effectiveTool.getItemHolder().unwrapKey().map(key -> key.location().toString()).orElse(null);
        
        Item item = block.asItem();
        if (item == Items.AIR) {
            return null;
        }
        ItemStack stack = new ItemStack(item);

        Float destroySpeed = null;
        try {
            destroySpeed = state.getDestroySpeed(null, null);
        } catch (Exception ignored) {
        }

        return new BlockMetadata(
            stack.getMaxStackSize(),
            effectiveToolId,
            block.getExplosionResistance(),
            destroySpeed,
            state.ignitedByLava()
        );
    }
}
