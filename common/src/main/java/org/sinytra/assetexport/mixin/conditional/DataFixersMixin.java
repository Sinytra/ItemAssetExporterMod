package org.sinytra.assetexport.mixin.conditional;

import com.mojang.datafixers.DataFixerBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(DataFixers.class)
public class DataFixersMixin {
    static {
        SharedConstants.CHECK_DATA_FIXER_SCHEMA = false;
    }

    // This is a performance optimisation, we're not interested in datafixers when dumping assets
    @Overwrite
    private static void addFixers(DataFixerBuilder builder) {

    }
}
