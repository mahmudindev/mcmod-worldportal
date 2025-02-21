package com.github.mahmudindev.minecraft.worldportal.config;

import com.github.mahmudindev.minecraft.worldportal.WorldPortal;
import com.github.mahmudindev.minecraft.worldportal.portal.PortalData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static Config CONFIG = new Config();

    private final Map<String, PortalData> portals = new HashMap<>();

    private void defaults() {
        PortalData portalData = new PortalData();
        portalData.setFrameTopRight("minecraft:iron_block");
        portalData.setFrameTopLeft("minecraft:gold_block");
        portalData.setFrameBottomRight("minecraft:gold_block");
        portalData.setFrameBottomLeft("minecraft:iron_block");
        portalData.setDestination(WorldPortal.MOD_ID + ":dimension");
        this.portals.put(WorldPortal.MOD_ID + ":portal", portalData);
    }

    public Map<String, PortalData> getPortals() {
        return Map.copyOf(this.portals);
    }

    private static Path getPath() {
        return WorldPortal.CONFIG_DIR.resolve(WorldPortal.MOD_ID + ".json");
    }

    private static File getFile() {
        return getPath().toFile();
    }

    public static void load() {
        Gson parser = new GsonBuilder().setPrettyPrinting().create();

        File configFile = getFile();
        if (!configFile.exists()) {
            CONFIG.defaults();

            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(parser.toJson(CONFIG));
            } catch (IOException e) {
                WorldPortal.LOGGER.error("Failed to write config", e);
            }
        } else {
            try (FileReader reader = new FileReader(configFile)) {
                CONFIG = parser.fromJson(reader, Config.class);
            } catch (IOException e) {
                WorldPortal.LOGGER.error("Failed to read config", e);
            }
        }
    }

    public static Config getConfig() {
        return CONFIG;
    }
}
