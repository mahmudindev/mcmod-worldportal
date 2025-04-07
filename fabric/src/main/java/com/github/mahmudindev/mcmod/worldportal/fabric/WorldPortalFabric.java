package com.github.mahmudindev.mcmod.worldportal.fabric;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class WorldPortalFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        WorldPortal.init();

        ResourceManagerHelper
                .get(PackType.SERVER_DATA)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return ResourceLocation.fromNamespaceAndPath(
                                WorldPortal.MOD_ID,
                                "default"
                        );
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        WorldPortal.onResourceManagerReload(resourceManager);
                    }
                });
    }
}
