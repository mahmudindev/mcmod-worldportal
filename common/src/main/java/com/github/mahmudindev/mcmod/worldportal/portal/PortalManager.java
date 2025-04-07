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
    private static final Map<ResourceLocation, PortalData> PORTALS = new HashMap<>();

    public static void onResourceManagerReload(ResourceManager manager) {
        PORTALS.clear();

        Config config = Config.getConfig();
        config.getPortals().forEach((id, portal) -> setPortal(
                ResourceLocation.parse(id),
                portal
        ));

        Gson gson = new Gson();
        manager.listResources(
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

                setPortal(resourceLocation.withPath(portalPath), gson.fromJson(
                        JsonParser.parseReader(resource.openAsReader()),
                        PortalData.class
                ));
            } catch (IOException e) {
                WorldPortal.LOGGER.error("Failed to read datapack", e);
            }
        });
    }

    public static Map<ResourceLocation, PortalData> getPortals() {
        return Map.copyOf(PORTALS);
    }

    public static PortalData getPortal(ResourceLocation id) {
        return PORTALS.get(id);
    }

    public static void setPortal(ResourceLocation id, PortalData portal) {
        PORTALS.put(id, portal);
    }
}
