package org.sinytra.assetexport.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.sinytra.assetexport.CommonClass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Queue;
import java.util.concurrent.Executors;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin extends ReentrantBlockableEventLoop<Runnable> {

    @Shadow
    private Connection pendingConnection;

    @Shadow
    @Final
    private Queue<Runnable> progressTasks;

    public MinecraftMixin(String name) {
        super(name);
    }

    @Inject(method = "onGameLoadFinished", at = @At("TAIL"))
    private void onResourceReloadComplete(CallbackInfo ci) {
        CommonClass.startRender();
        setOverlay(null);

        if (CommonClass.loadWorld) {
            var levelName = "assetdump";
            var minecraft = (Minecraft) (Object) this;
            if (!minecraft.getLevelSource().levelExists(levelName)) {
                minecraft.createWorldOpenFlows().createFreshLevel(levelName, new LevelSettings(levelName, GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(levelName.hashCode(), false, false), access -> access.registryOrThrow(Registries.WORLD_PRESET)
                                .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), null);
            } else {
                minecraft.createWorldOpenFlows().openWorld(levelName, () -> minecraft.setScreen(new TitleScreen()));
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "runTick", cancellable = true)
    private void captureTick(boolean renderLevel, CallbackInfo ci) {
        if (CommonClass.mainThread != null) {
            if (renderLevel) {
                this.runAllTasks();

                Runnable runnable;
                while ((runnable = this.progressTasks.poll()) != null) {
                    runnable.run();
                }

                if (pendingConnection != null) {
                    pendingConnection.tick();
                }
            }

            CommonClass.mainThread.run();

            ci.cancel();
        }
    }

    @Shadow
    public abstract void setOverlay(@Nullable Overlay loadingGui);
}
