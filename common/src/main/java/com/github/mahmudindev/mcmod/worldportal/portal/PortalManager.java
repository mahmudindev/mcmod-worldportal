package com.github.mahmudindev.mcmod.worldportal.portal;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.github.mahmudindev.mcmod.worldportal.config.Config;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PortalManager {
    private static final Map<Identifier, PortalConfig> PORTAL_CONFIGS = new HashMap<>();

    public static void onResourceManagerReload(ResourceManager manager) {
        PORTAL_CONFIGS.clear();

        Config config = Config.getConfig();
        config.getPortals().forEach((id, portal) -> setPortalConfig(
                Identifier.parse(id),
                portal
        ));

        Gson gson = new Gson();
        manager.listResources(
                WorldPortal.MOD_ID,
                identifier -> identifier.getPath().endsWith(".json")
        ).forEach((identifier, resource) -> {
            String resourcePath = identifier.getPath().replaceFirst(
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

                setPortalConfig(identifier.withPath(portalPath), gson.fromJson(
                        JsonParser.parseReader(resource.openAsReader()),
                        PortalConfig.class
                ));
            } catch (IOException e) {
                WorldPortal.LOGGER.error("Failed to read datapack", e);
            }
        });
    }

    public static Map<Identifier, PortalConfig> getPortalConfigs() {
        return Map.copyOf(PORTAL_CONFIGS);
    }

    public static PortalConfig getPortalConfig(Identifier id) {
        return PORTAL_CONFIGS.get(id);
    }

    public static void setPortalConfig(Identifier id, PortalConfig portalConfig) {
        PORTAL_CONFIGS.put(id, portalConfig);
    }
}
