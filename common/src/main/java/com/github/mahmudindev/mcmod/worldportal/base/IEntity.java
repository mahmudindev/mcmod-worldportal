package com.github.mahmudindev.mcmod.worldportal.base;

import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public interface IEntity {
    ResourceLocation worldportal$getPortalId();

    PortalData worldportal$getPortal();

    void worldportal$setPortal(ResourceLocation portalId);

    ResourceKey<Level> worldportal$setupPortal(
            BlockPos blockPos,
            ResourceKey<Level> originalKey
    );
}
