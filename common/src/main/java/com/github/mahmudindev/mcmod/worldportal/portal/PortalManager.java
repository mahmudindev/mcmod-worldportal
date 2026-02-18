package com.github.mahmudindev.mcmod.worldportal.portal;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.github.mahmudindev.mcmod.worldportal.config.Config;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PortalManager {
    private static final Map<ResourceLocation, PortalConfig> PORTAL_CONFIGS = new HashMap<>();

    public static void onResourceManagerReload(ResourceManager resourceManager) {
        PORTAL_CONFIGS.clear();

        Config config = Config.getConfig();
        config.getPortals().forEach((id, portal) -> setPortalConfig(
                new ResourceLocation(id),
                portal
        ));

        Gson gson = new Gson();
        resourceManager.listResources(
                WorldPortal.MOD_ID,
                resourceLocation -> resourceLocation.getPath().endsWith(".json")
        ).forEach((resourceLocation, resource) -> {
            String resourcePath = resourceLocation.getPath().replaceFirst(
                    "^%s/".formatted(WorldPortal.MOD_ID),
                    ""
            );

            if (!resourcePath.startsWith("portal/")) {
                return;
            }

            try {
                String portalPath = resourcePath
                        .substring(resourcePath.indexOf("/") + 1)
                        .replaceAll("\\.json$", "");

                setPortalConfig(resourceLocation.withPath(portalPath), gson.fromJson(
                        JsonParser.parseReader(resource.openAsReader()),
                        PortalConfig.class
                ));
            } catch (IOException e) {
                WorldPortal.LOGGER.error("Failed to read datapack", e);
            }
        });
    }

    public static Map<ResourceLocation, PortalConfig> getPortalConfigs() {
        return Map.copyOf(PORTAL_CONFIGS);
    }

    public static PortalConfig getPortalConfig(ResourceLocation id) {
        return PORTAL_CONFIGS.get(id);
    }

    public static void setPortalConfig(ResourceLocation id, PortalConfig portalConfig) {
        PORTAL_CONFIGS.put(id, portalConfig);
    }
}
