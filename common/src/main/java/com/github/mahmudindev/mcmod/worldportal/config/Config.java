package com.github.mahmudindev.mcmod.worldportal.config;

import com.github.mahmudindev.mcmod.worldportal.WorldPortalExpectPlatform;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
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
    private static final Path CONFIG_DIR = WorldPortalExpectPlatform.getConfigDir();
    private static Config CONFIG = new Config();

    private final Map<String, PortalData> portals = new HashMap<>();

    private void defaults() {
        PortalData portal0 = new PortalData();
        portal0.setFrameTopRight("minecraft:gold_block");
        portal0.setFrameTopLeft("minecraft:iron_block");
        portal0.setFrameBottomRight("minecraft:iron_block");
        portal0.setFrameBottomLeft("minecraft:gold_block");
        portal0.setMode(String.valueOf(PortalData.DEFAULT_MODE));
        portal0.setDestination(WorldPortal.MOD_ID + ":dimension");
        this.portals.put(WorldPortal.MOD_ID + ":default", portal0);

        PortalData portal1 = new PortalData();
        portal1.setFrameTopRight("minecraft:gold_block");
        portal1.setFrameTopLeft("minecraft:iron_block");
        portal1.setFrameBottomRight("minecraft:iron_block");
        portal1.setFrameBottomLeft("minecraft:gold_block");
        portal1.setMode(String.valueOf(PortalData.DEFAULT_MODE));
        portal1.setDestination(WorldPortal.MOD_ID + ":dimension");
        this.portals.put(WorldPortal.MOD_ID + ":horizontal", portal1);
    }

    public Map<String, PortalData> getPortals() {
        return Map.copyOf(this.portals);
    }

    public static void load() {
        Gson parser = new GsonBuilder().setPrettyPrinting().create();

        File configFile = CONFIG_DIR.resolve(WorldPortal.MOD_ID + ".json").toFile();
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
