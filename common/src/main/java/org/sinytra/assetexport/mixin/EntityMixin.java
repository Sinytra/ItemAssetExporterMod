package org.sinytra.assetexport.mixin;

import net.minecraft.world.entity.Entity;
import org.sinytra.assetexport.dumper.Identifiable;
import org.sinytra.assetexport.dumper.IdentifiableType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public class EntityMixin implements Identifiable<Entity> {
    @Override
    public IdentifiableType<Entity> getIdentifiableType() {
        return ((Entity) (Object) this).getType();
    }

    @Override
    public String representAsString() {
        return "entity (" + getIdentifiableType().getId() + ")";
    }
}
