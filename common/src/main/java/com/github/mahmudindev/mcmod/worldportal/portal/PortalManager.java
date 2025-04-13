package com.github.mahmudindev.mcmod.worldportal.portal;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.config.Config;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

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

    public static void onPlayerBreakPortal(Level level, BlockPos pos, BlockState state) {
        if (!state.is(Blocks.NETHER_PORTAL)) {
            return;
        }

        if (level instanceof IServerLevel serverLevelX) {
            BlockUtil.FoundRectangle foundRectangle = BlockUtil.getLargestRectangleAround(
                    pos,
                    state.getValue(BlockStateProperties.HORIZONTAL_AXIS),
                    21,
                    Direction.Axis.Y,
                    21,
                    blockPos -> level.getBlockState(blockPos) == state
            );

            PortalReturns portalReturns = serverLevelX.worldportal$getPortalReturns();
            portalReturns.removeDimension(foundRectangle.minCorner);
        }
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
