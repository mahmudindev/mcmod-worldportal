package com.github.mahmudindev.mcmod.worldportal.forge;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class WorldPortalExpectPlatformImpl {
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
