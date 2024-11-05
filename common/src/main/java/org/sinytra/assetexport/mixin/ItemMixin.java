package org.sinytra.assetexport.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.sinytra.assetexport.dumper.IdentifiableType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.class)
public abstract class ItemMixin implements IdentifiableType<ItemStack> {
    @Shadow
    @Final
    private Holder.Reference<Item> builtInRegistryHolder;

    @Override
    public ResourceLocation getId() {
        return builtInRegistryHolder.key().location();
    }

    @Shadow
    public abstract ItemStack getDefaultInstance();

    @Override
    public ItemStack defaultIdentifiable() {
        return getDefaultInstance();
    }
}
