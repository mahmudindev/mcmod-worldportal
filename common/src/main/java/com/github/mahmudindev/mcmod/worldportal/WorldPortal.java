package com.github.mahmudindev.mcmod.worldportal;

import com.github.mahmudindev.mcmod.worldportal.config.Config;
import com.github.mahmudindev.mcmod.worldportal.platform.Services;
import com.github.mahmudindev.mcmod.worldportal.platform.services.IPlatformHelper;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public final class WorldPortal {
    public static final String MOD_ID = "worldportal";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final IPlatformHelper PLATFORM = Services.PLATFORM;

    public static void init() {
        Config.load();
    }

    public static void onResourceManagerReload(ResourceManager resourceManager) {
        PortalManager.onResourceManagerReload(resourceManager);
    }
}
