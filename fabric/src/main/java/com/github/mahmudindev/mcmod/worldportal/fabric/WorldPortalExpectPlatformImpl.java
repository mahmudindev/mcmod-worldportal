package com.github.mahmudindev.mcmod.worldportal.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class WorldPortalExpectPlatformImpl {
    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
