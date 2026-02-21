package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalReturns;
import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DimensionDataStorage;
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
    private PortalReturns portalReturns;
    @Unique
    private PortalData portalData;

    @Shadow public abstract DimensionDataStorage getDataStorage();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initLast(
            MinecraftServer server,
            Executor executor,
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            ServerLevelData serverLevelData,
            ResourceKey<Level> dimension,
            LevelStem levelStem,
            ChunkProgressListener chunkProgressListener,
            boolean isDebug,
            long biomeZoomSeed,
            List<CustomSpawner> customSpawners,
            boolean tickTime,
            RandomSequences randomSequences,
            CallbackInfo ci
    ) {
        this.portalData = this.getDataStorage().computeIfAbsent(
                PortalData::load,
                PortalData::new,
                PortalData.FIELD
        );
        this.portalReturns = this.getDataStorage().computeIfAbsent(
                v -> PortalReturns.migrate(v, this.portalData),
                PortalReturns::new,
                PortalReturns.FIELD
        );
    }

    @Override
    public PortalReturns worldportal$getPortalReturns() {
        return this.portalReturns;
    }

    @Override
    public PortalData worldportal$getPortalData() {
        return this.portalData;
    }
}
