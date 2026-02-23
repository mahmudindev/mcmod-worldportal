package com.github.mahmudindev.mcmod.worldportal.base;

import com.github.mahmudindev.mcmod.worldportal.portal.PortalConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public interface IEntity {
    Identifier worldportal$getPortalId();

    PortalConfig worldportal$getPortalConfig();

    void worldportal$setPortal(Identifier portalId);

    ResourceKey<Level> worldportal$setupPortal(
            BlockPos blockPos,
            ResourceKey<Level> originalKey
    );
}
