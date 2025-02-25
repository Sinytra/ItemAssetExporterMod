package org.sinytra.assetexport.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.sinytra.assetexport.CommonClass;
import org.sinytra.assetexport.dumper.IdentifiableType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements IdentifiableType<Entity> {
    @Shadow
    @Final
    private Holder.Reference<EntityType<?>> builtInRegistryHolder;

    @Override
    public ResourceLocation getId() {
        return builtInRegistryHolder.key().location();
    }

    @Override
    public Entity defaultIdentifiable() {
        return ((EntityType) (Object) this).create(Minecraft.getInstance().getSingleplayerServer().overworld());
    }
}
