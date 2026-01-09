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
    private static final Map<Identifier, PortalData> PORTALS = new HashMap<>();

    public static void onResourceManagerReload(ResourceManager manager) {
        PORTALS.clear();

        Config config = Config.getConfig();
        config.getPortals().forEach((id, portal) -> setPortal(
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

                setPortal(identifier.withPath(portalPath), gson.fromJson(
                        JsonParser.parseReader(resource.openAsReader()),
                        PortalData.class
                ));
            } catch (IOException e) {
                WorldPortal.LOGGER.error("Failed to read datapack", e);
            }
        });
    }

    public static Map<Identifier, PortalData> getPortals() {
        return Map.copyOf(PORTALS);
    }

    public static PortalData getPortal(Identifier id) {
        return PORTALS.get(id);
    }

    public static void setPortal(Identifier id, PortalData portal) {
        PORTALS.put(id, portal);
    }
}
