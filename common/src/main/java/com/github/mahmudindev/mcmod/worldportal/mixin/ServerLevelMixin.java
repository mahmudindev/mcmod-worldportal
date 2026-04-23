package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements IServerLevel {
    @Unique
    private PortalData portalData;

    @Shadow public abstract SavedDataStorage getDataStorage();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initLast(
            MinecraftServer server,
            Executor executor,
            LevelStorageSource.LevelStorageAccess levelStorage,
            ServerLevelData levelData,
            ResourceKey<Level> dimension,
            LevelStem levelStem,
            boolean isDebug,
            long biomeZoomSeed,
            List<CustomSpawner> customSpawners,
            boolean tickTime,
            CallbackInfo ci
    ) {
        this.portalData = this.getDataStorage().computeIfAbsent(PortalData.TYPE);
    }

    @Override
    public PortalData worldportal$getPortalData() {
        return this.portalData;
    }
}
