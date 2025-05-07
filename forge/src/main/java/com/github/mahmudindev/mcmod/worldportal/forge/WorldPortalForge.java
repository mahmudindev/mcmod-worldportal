package com.github.mahmudindev.mcmod.worldportal.forge;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(WorldPortal.MOD_ID)
public final class WorldPortalForge {
    public WorldPortalForge() {
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

        MinecraftForge.EVENT_BUS.addListener((BlockEvent.BreakEvent event) -> {
            LevelAccessor levelAccessor = event.getLevel();
            if (levelAccessor instanceof Level level) {
                PortalManager.onPlayerBreakPortal(level, event.getPos(), event.getState());
            }
        });
    }
}
