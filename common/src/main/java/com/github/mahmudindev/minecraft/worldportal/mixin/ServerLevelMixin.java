package com.github.mahmudindev.minecraft.worldportal.mixin;

import com.github.mahmudindev.minecraft.worldportal.base.IServerLevel;
import com.github.mahmudindev.minecraft.worldportal.portal.PortalData;
import com.github.mahmudindev.minecraft.worldportal.portal.PortalReturns;
import net.minecraft.core.BlockPos;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements IServerLevel {
    @Unique private final Map<BlockPos, BlockPos> portalIP = new HashMap<>();
    @Unique private final Map<BlockPos, PortalData> portalID = new HashMap<>();
    @Unique private PortalReturns portalReturns;

    @Shadow public abstract MinecraftServer getServer();
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
            long l,
            List<CustomSpawner> customSpawners,
            boolean tickTime,
            RandomSequences randomSequences,
            CallbackInfo ci
    ) {
        this.portalReturns = this.getDataStorage().computeIfAbsent(
                PortalReturns::load,
                PortalReturns::new,
                PortalReturns.FIELD
        );
    }

    @Override
    public BlockPos worldportal$getPortalInfoPos(BlockPos pos) {
        return this.portalIP.get(pos);
    }

    @Override
    public PortalData worldportal$getPortalInfoData(BlockPos pos) {
        return this.portalID.get(pos);
    }

    @Override
    public BlockPos worldportal$setPortalInfo(
            ResourceKey<Level> dimension,
            BlockPos pos,
            PortalData portalData
    ) {
        BlockPos posX = pos.offset(0, -42069, 0);
        for (ResourceKey<Level> v : this.getServer().levelKeys()) {
            if (v == dimension) {
                break;
            }

            posX = posX.offset(0, -1, 0);
        }

        this.portalIP.put(posX, pos);
        this.portalID.put(posX, portalData);

        return posX;
    }

    @Override
    public void worldportal$removePortalInfo(BlockPos pos) {
        this.portalIP.remove(pos);
        this.portalID.remove(pos);
    }

    @Override
    public PortalReturns worldportal$getPortalReturns() {
        return this.portalReturns;
    }
}
