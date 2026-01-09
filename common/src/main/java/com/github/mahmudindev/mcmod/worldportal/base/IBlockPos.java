package com.github.mahmudindev.mcmod.worldportal.base;

import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public interface IBlockPos {
    Level worldportal$getLevel();

    void worldportal$setLevel(Level level);

    BlockPos worldportal$getPortalEntrancePos();

    void worldportal$setPortalEntrancePos(BlockPos portalEntrancePos);

    Identifier worldportal$getPortalId();

    PortalData worldportal$getPortal();

    void worldportal$setPortal(Identifier portalId);
}
