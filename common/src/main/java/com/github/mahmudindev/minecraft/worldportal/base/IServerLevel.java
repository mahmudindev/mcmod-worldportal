package com.github.mahmudindev.minecraft.worldportal.base;

import com.github.mahmudindev.minecraft.worldportal.portal.PortalData;
import com.github.mahmudindev.minecraft.worldportal.portal.PortalReturns;
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
