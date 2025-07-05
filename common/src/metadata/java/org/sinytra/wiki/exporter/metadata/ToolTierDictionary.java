package org.sinytra.wiki.exporter.metadata;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.stream.Stream;

public class ToolTierDictionary {
    private static final List<ItemStack> TOOLS = Stream.of(
            // Pickaxe
            Items.WOODEN_PICKAXE, Items.GOLDEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE,
            // Axe
            Items.WOODEN_AXE, Items.GOLDEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE,
            // Shovel
            Items.WOODEN_SHOVEL, Items.GOLDEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL,
            // Hoe
            Items.WOODEN_HOE, Items.GOLDEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE,
            // Sword
            Items.WOODEN_SWORD
        )
        .map(ItemStack::new)
        .toList();

    // Source: https://github.com/Snownee/Jade/blob/00821ac06f49aba3536a9be9f2839125cb1c1543/src/main/java/snownee/jade/addon/harvest/SimpleToolHandler.java#L18
    public static ItemStack getEffectiveTool(BlockState state) {
        try {
            if (!state.requiresCorrectToolForDrops() && state.getDestroySpeed(null, null) == 0) {
                return ItemStack.EMPTY;
            }
        } catch (Exception ignored) {
        }

        tools:
        for (ItemStack toolItem : TOOLS) {
            Tool tool = toolItem.get(DataComponents.TOOL);

            for (Tool.Rule rule : tool.rules()) {
                if (rule.correctForDrops().isPresent() && state.is(rule.blocks())) {
                    if (rule.correctForDrops().get()) {
                        return toolItem;
                    }
                    continue tools;
                }
            }

            if (tool.getMiningSpeed(state) > tool.defaultMiningSpeed() || toolItem.isCorrectToolForDrops(state)) {
                return toolItem;
            }
        }

        return ItemStack.EMPTY;
    }
}
