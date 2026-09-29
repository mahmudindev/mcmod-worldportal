package com.github.mahmudindev.mcmod.worldportal;

import com.github.mahmudindev.mcmod.orenoevents.event.events.ServerEvents;
import com.github.mahmudindev.mcmod.worldportal.config.Config;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class WorldPortal {
    public static final String MOD_ID = "worldportal";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        Config.load();

        ServerEvents.RESOURCE_MANAGER_RELOAD.register(resourceManager -> {
            PortalManager.onServerResourceManagerReload(resourceManager);
        });
    }
}
