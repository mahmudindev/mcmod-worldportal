package com.github.mahmudindev.mcmod.worldportal.neoforge;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class WorldPortalExpectPlatformImpl {
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
