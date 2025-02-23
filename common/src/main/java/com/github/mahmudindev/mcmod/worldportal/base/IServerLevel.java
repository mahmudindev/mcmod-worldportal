package com.github.mahmudindev.mcmod.worldportal.base;

import com.github.mahmudindev.mcmod.worldportal.portal.PortalReturns;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IServerLevel {
    BlockPos worldportal$getPortalInfoPos(BlockPos pos);

    PortalData worldportal$getPortalInfoData(BlockPos pos);

    BlockPos worldportal$setPortalInfo(
            ResourceKey<Level> dimension,
            BlockPos pos,
            PortalData portalData
    );

    void worldportal$removePortalInfo(BlockPos pos);

    PortalReturns worldportal$getPortalReturns();
}
