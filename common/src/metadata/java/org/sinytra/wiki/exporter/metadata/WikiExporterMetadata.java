package org.sinytra.wiki.exporter.metadata;

import com.google.common.base.Suppliers;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.sinytra.wiki.exporter.platform.services.ExporterModule;
import org.sinytra.wiki.exporter.util.EnumToLowerCaseJsonConverter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class WikiExporterMetadata implements ExporterModule {
    private static final Supplier<AttributeSupplier> PLAYER_ATTRIBUTES = Suppliers.memoize(() -> Player.createAttributes().build());
    private static final String LEGACY_PROPS_PATH = "properties.json";
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeHierarchyAdapter(Enum.class, new EnumToLowerCaseJsonConverter())
        .create();

    private final Set<String> namespaces;

    public WikiExporterMetadata(Set<String> namespaces) {
        this.namespaces = namespaces;
    }

    @Override
    public void run(Path output) throws Exception {
        if (this.namespaces.isEmpty()) {
            return;
        }

        boolean namespaced = Boolean.getBoolean("wiki_exporter.module.%s.namespaced".formatted(WikiMetadataModuleFactory.NAME));
        String itemPropsPath = Objects.requireNonNullElse(
            System.getProperty("wiki_exporter.module.%s.output.items".formatted(WikiMetadataModuleFactory.NAME)),
            LEGACY_PROPS_PATH
        );

        Map<String, Map<String, Object>> metadata = new HashMap<>();

        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            Identifier name = entry.getKey().identifier();
            Item block = entry.getValue();
            if (!this.namespaces.contains(name.getNamespace())) {
                continue;
            }

            ItemMetadata data = getItemMetadata(block);
            if (data == null) {
                continue;
            }
            putMetadata(metadata, name, data);
        }

        for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
            Identifier name = entry.getKey().identifier();
            Block block = entry.getValue();
            if (!this.namespaces.contains(name.getNamespace())) {
                continue;
            }

            BlockMetadata data = getBlockMetadata(block);
            if (data == null) {
                continue;
            }
            putMetadata(metadata, name, data);
        }

        if (namespaced) {
            for (Entry<String, Map<String, Object>> entry : metadata.entrySet()) {
                String namespace = entry.getKey();
                String content = GSON.toJson(entry.getValue());

                Path outputPath = output.resolve(namespace).resolve(itemPropsPath);
                Files.createDirectories(outputPath.getParent());
                Files.writeString(outputPath, content, StandardCharsets.UTF_8);
            }
        } else {
            Map<String, Object> flat = new TreeMap<>();
            metadata.values().forEach(flat::putAll);
            String content = GSON.toJson(flat);

            Path outputPath = output.resolve(itemPropsPath);
            Files.createDirectories(outputPath.getParent());
            Files.writeString(outputPath, content, StandardCharsets.UTF_8);
        }
    }

    private static void putMetadata(Map<String, Map<String, Object>> map, Identifier id, Object value) {
        map.computeIfAbsent(id.getNamespace(), n -> new TreeMap<>())
            .put(id.toString(), value);
    }

    record BlockMetadata(
        int stackSize,
        String requiredTool,
        float blastResistance,
        Float hardness,
        boolean flammable
    ) {
    }

    record ItemMetadata(
        // Generic
        int stackSize,
        Rarity rarity,
        // Tools
        Integer durability,
        Float miningSpeed,
        Float attackDamage,
        Float attackSpeed,
        Integer enchantability,
        // Food
        Integer nutrition
    ) {
    }

    private static BlockMetadata getBlockMetadata(Block block) {
        BlockState state = block.defaultBlockState();
        ItemStack effectiveTool = ToolTierDictionary.getEffectiveTool(state);
        String effectiveToolId = effectiveTool.isEmpty() ? null : effectiveTool
            .getItem()
            .builtInRegistryHolder()
            .unwrapKey().map(key -> key.identifier().toString()).orElse(null);

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

    private static ItemMetadata getItemMetadata(Item item) {
        ItemStack stack = item.getDefaultInstance();

        // Common
        Integer maxStackSize = stack.getMaxStackSize();
        Rarity rarity = stack.getRarity();

        // Food
        FoodProperties food = stack.get(DataComponents.FOOD);
        Integer nutrition = food != null ? food.nutrition() : null;

        // Tools
        Tool tool = stack.get(DataComponents.TOOL);
        Integer durability = stack.getMaxDamage() == 0 ? null : stack.getMaxDamage();
        Float miningSpeed = tool != null ? tool.rules().stream()
            .map(Tool.Rule::speed).filter(Optional::isPresent).map(Optional::get)
            .findFirst()
            .orElse(null)
            : null;
        Enchantable enchantable = stack.get(DataComponents.ENCHANTABLE);
        Integer enchantability = enchantable != null ? enchantable.value() : null;

        // Weapons
        Float attackDamage = computeAttributeModifierValue(stack, Attributes.ATTACK_DAMAGE, Item.BASE_ATTACK_DAMAGE_ID);
        Float attackSpeed = computeAttributeModifierValue(stack, Attributes.ATTACK_SPEED, Item.BASE_ATTACK_SPEED_ID);

        return new ItemMetadata(
            // Common
            maxStackSize,
            rarity,
            // Tools
            durability,
            miningSpeed,
            // Weapons
            attackDamage,
            attackSpeed,
            enchantability,
            // Food
            nutrition
        );
    }

    @Nullable
    private static Float computeAttributeModifierValue(ItemStack stack, Holder<Attribute> attribute, Identifier id) {
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers != null) {
            double playerBase = PLAYER_ATTRIBUTES.get().getBaseValue(attribute);

            AtomicBoolean found = new AtomicBoolean(false);
            AtomicReference<Double> base = new AtomicReference<>(playerBase);
            AtomicReference<Double> amt = new AtomicReference<>(base.get());

            modifiers.forEach(EquipmentSlot.MAINHAND, (a, m) -> {
                if (m.id().equals(id)) {
                    found.set(true);

                    switch (m.operation()) {
                        case ADD_VALUE:
                            base.set(amt.updateAndGet(d -> d + m.amount()));
                            break;
                        case ADD_MULTIPLIED_BASE:
                            amt.updateAndGet(d -> m.amount() * base.get());
                            break;
                        case ADD_MULTIPLIED_TOTAL:
                            amt.updateAndGet(d -> d * (1 + m.amount()));
                            break;
                    }
                }
            });

            if (found.get()) {
                float raw = amt.get().floatValue();
                return Math.round(raw * 10) / 10.0f;
            }
        }
        return null;
    }
}
