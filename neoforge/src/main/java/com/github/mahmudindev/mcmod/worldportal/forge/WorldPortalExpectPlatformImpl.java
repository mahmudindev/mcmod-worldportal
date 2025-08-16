package com.github.mahmudindev.mcmod.worldportal.forge;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.RegisterEvent;

import java.nio.file.Path;
import java.util.Set;

public class WorldPortalExpectPlatformImpl {
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static void registerPoiType(
            ResourceKey<PoiType> resourceKey,
            Set<BlockState> matchingStates,
            int maxTickets,
            int validRange
    ) {
        // When using the constructor, it crashes on the latest v47.1
        //noinspection removal
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        eventBus.addListener((RegisterEvent event) -> {
            event.register(
                    Registries.POINT_OF_INTEREST_TYPE,
                    registry -> {
                        registry.register(
                                resourceKey,
                                new PoiType(matchingStates, maxTickets,  validRange)
                        );
                    }
            );
        });
    }
}
