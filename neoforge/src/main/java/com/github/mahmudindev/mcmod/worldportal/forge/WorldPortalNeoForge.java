package com.github.mahmudindev.mcmod.worldportal.forge;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(WorldPortal.MOD_ID)
public final class WorldPortalNeoForge {
    public WorldPortalNeoForge() {
        // Run our common setup.
        WorldPortal.init();

        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> {
            event.addListener(new ResourceManagerReloadListener() {
                @Override
                public void onResourceManagerReload(ResourceManager resourceManager) {
                    WorldPortal.onResourceManagerReload(resourceManager);
                }
            });
        });
    }
}
