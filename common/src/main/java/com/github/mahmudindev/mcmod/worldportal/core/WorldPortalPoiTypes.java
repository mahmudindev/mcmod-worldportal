package com.github.mahmudindev.mcmod.worldportal.core;

import com.github.mahmudindev.mcmod.worldportal.WorldPortalExpectPlatform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;

public class WorldPortalPoiTypes {
    public static final ResourceKey<PoiType> END_PORTAL = ResourceKey.create(
            Registries.POINT_OF_INTEREST_TYPE,
            Identifier.withDefaultNamespace("end_portal")
    );

    public static void bootstrap() {
        if (BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValue(END_PORTAL) == null) {
            WorldPortalExpectPlatform.registerPoiType(
                    END_PORTAL,
                    Set.copyOf(Blocks.END_PORTAL.getStateDefinition().getPossibleStates()),
                    0,
                    1
            );
        }
    }
}
