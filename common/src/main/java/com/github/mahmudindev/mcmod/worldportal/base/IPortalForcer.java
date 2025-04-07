package com.github.mahmudindev.mcmod.worldportal.base;

import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface IPortalForcer {
    PortalData worldportal$getPortal(BlockPos virtualPos);

    BlockPos worldportal$setPortal(BlockPos pos, ResourceLocation id);

    void worldportal$removePortal(BlockPos virtualPos);
}
