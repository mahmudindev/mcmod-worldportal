package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IBlockPos;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockPos.class)
public abstract class BlockPosMixin implements IBlockPos {
    @Unique
    private Level level;
    @Unique
    private BlockPos portalEntrancePos;
    @Unique
    private ResourceLocation portalId;

    @Override
    public Level worldportal$getLevel() {
        return this.level;
    }

    @Override
    public void worldportal$setLevel(Level level) {
        this.level = level;
    }

    @Override
    public BlockPos worldportal$getPortalEntrancePos() {
        return this.portalEntrancePos;
    }

    @Override
    public void worldportal$setPortalEntrancePos(BlockPos portalEntrancePos) {
        this.portalEntrancePos = portalEntrancePos;
    }

    @Override
    public ResourceLocation worldportal$getPortalId() {
        return this.portalId;
    }

    @Override
    public PortalData worldportal$getPortal() {
        return PortalManager.getPortal(this.worldportal$getPortalId());
    }

    @Override
    public void worldportal$setPortal(ResourceLocation portalId) {
        this.portalId = portalId;
    }
}
