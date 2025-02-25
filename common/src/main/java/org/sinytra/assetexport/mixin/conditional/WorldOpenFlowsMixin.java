package org.sinytra.assetexport.mixin.conditional;

import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {
    @Overwrite
    private void askForBackup(LevelStorageSource.LevelStorageAccess levelStorage, boolean customized, Runnable loadLevel, Runnable onCancel) {
        loadLevel.run();
    }
}
