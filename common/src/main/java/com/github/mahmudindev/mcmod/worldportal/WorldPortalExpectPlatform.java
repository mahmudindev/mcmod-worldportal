package com.github.mahmudindev.mcmod.worldportal;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

public class WorldPortalExpectPlatform {
    @ExpectPlatform
    public static Path getConfigDir() {
        return Path.of(".");
    }
}
