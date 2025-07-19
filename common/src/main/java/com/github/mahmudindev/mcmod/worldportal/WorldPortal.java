package com.github.mahmudindev.mcmod.worldportal;

import com.github.mahmudindev.mcmod.worldportal.config.Config;
import com.github.mahmudindev.mcmod.worldportal.core.WorldPortalPoiTypes;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public final class WorldPortal {
    public static final String MOD_ID = "worldportal";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        Config.load();

        WorldPortalPoiTypes.bootstrap();
    }

    public static void onResourceManagerReload(ResourceManager resourceManager) {
        PortalManager.onResourceManagerReload(resourceManager);
    }
}
