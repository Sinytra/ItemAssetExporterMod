package org.sinytra.assetexport.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.sinytra.assetexport.dumper.Identifiable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.class)
public class ItemMixin implements Identifiable {
    @Shadow
    @Final
    private Holder.Reference<Item> builtInRegistryHolder;

    @Override
    public ResourceLocation getId() {
        return builtInRegistryHolder.key().location();
    }
}
