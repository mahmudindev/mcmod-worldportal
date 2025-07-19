package com.github.mahmudindev.mcmod.worldportal.neoforge;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@Mod(WorldPortal.MOD_ID)
public final class WorldPortalNeoForge {
    public static IEventBus EVENT_BUS;

    public WorldPortalNeoForge(IEventBus eventBus) {
        EVENT_BUS = eventBus;

        // Run our common setup.
        WorldPortal.init();

        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> {
            event.addListener(new ResourceManagerReloadListener() {
                @Override
                public void onResourceManagerReload(ResourceManager resourceManager) {
                    WorldPortal.onResourceManagerReload(resourceManager);
                }
            });
        });
    }
}
