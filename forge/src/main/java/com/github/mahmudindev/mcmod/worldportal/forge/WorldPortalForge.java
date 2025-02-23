package com.github.mahmudindev.mcmod.worldportal.forge;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(WorldPortal.MOD_ID)
public final class WorldPortalForge {
    public WorldPortalForge() {
        WorldPortal.CONFIG_DIR = FMLPaths.CONFIGDIR.get();

        // Run our common setup.
        WorldPortal.init();

        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> {
            event.addListener(this.onResourceManagerReload());
        });
    }

    private ResourceManagerReloadListener onResourceManagerReload() {
        return WorldPortal::onResourceManagerReload;
    }
}
