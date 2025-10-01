package com.github.mahmudindev.mcmod.worldportal.neoforge;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

@Mod(WorldPortal.MOD_ID)
public final class WorldPortalNeoForge {
    public static IEventBus EVENT_BUS;

    public WorldPortalNeoForge(IEventBus eventBus) {
        EVENT_BUS = eventBus;

        // Run our common setup.
        WorldPortal.init();

        NeoForge.EVENT_BUS.addListener((AddServerReloadListenersEvent event) -> {
            event.addListener(
                    ResourceLocation.fromNamespaceAndPath(
                            WorldPortal.MOD_ID,
                            "default"
                    ),
                    new ResourceManagerReloadListener() {
                        @Override
                        public void onResourceManagerReload(ResourceManager resourceManager) {
                            WorldPortal.onResourceManagerReload(resourceManager);
                        }
                    }
            );
        });
    }
}
