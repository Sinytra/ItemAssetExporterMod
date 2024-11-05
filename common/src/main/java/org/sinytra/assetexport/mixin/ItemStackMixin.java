package org.sinytra.assetexport.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.sinytra.assetexport.dumper.Identifiable;
import org.sinytra.assetexport.dumper.IdentifiableType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements Identifiable<ItemStack> {

    @Shadow
    public abstract Item getItem();

    @Override
    public IdentifiableType<ItemStack> getIdentifiableType() {
        return getItem();
    }

    @Override
    public String representAsString() {
        return "item " + getIdentifiableType().getId();
    }
}
