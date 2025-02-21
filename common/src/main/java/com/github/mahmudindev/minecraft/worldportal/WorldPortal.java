package com.github.mahmudindev.minecraft.worldportal;

import com.github.mahmudindev.minecraft.worldportal.config.Config;
import com.github.mahmudindev.minecraft.worldportal.portal.PortalManager;
import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.nio.file.Path;

public final class WorldPortal {
    public static final String MOD_ID = "worldportal";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static Path CONFIG_DIR = Path.of(".");

    public static void init() {
        Config.load();
    }

    public static void onResourceManagerReload(ResourceManager manager) {
        PortalManager.onResourceManagerReload(manager);
    }
}
