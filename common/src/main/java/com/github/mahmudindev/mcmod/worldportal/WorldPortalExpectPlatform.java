package com.github.mahmudindev.mcmod.worldportal;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.Set;

public class WorldPortalExpectPlatform {
    @ExpectPlatform
    public static Path getConfigDir() {
        return Path.of(".");
    }

    @ExpectPlatform
    public static void registerPoiType(
            ResourceKey<PoiType> resourceKey,
            Set<BlockState> matchingStates,
            int maxTickets,
            int validRange
    ) {
        return;
    }
}
