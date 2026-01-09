package com.github.mahmudindev.mcmod.worldportal.fabric;

import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.Set;

public class WorldPortalExpectPlatformImpl {
    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static void registerPoiType(
            ResourceKey<PoiType> resourceKey,
            Set<BlockState> matchingStates,
            int maxTickets,
            int validRange
    ) {
        PointOfInterestHelper.register(
                resourceKey.identifier(),
                maxTickets,
                validRange,
                matchingStates
        );
    }
}
