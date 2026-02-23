package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IBlockPos;
import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {
    @WrapOperation(
            method = "getExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalForcer;findClosestPortalPosition(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Ljava/util/Optional;"
            )
    )
    private Optional<BlockPos> getExitPortalForcerFindPrepare(
            PortalForcer instance,
            BlockPos blockPos,
            boolean isNether,
            WorldBorder worldBorder,
            Operation<Optional<BlockPos>> original,
            ServerLevel serverLevel,
            Entity entity,
            BlockPos blockPos2
    ) {
        IEntity entityX = (IEntity) entity;

        PortalConfig portalConfig = entityX.worldportal$getPortalConfig();
        if (portalConfig != null) {
            ((IBlockPos) blockPos).worldportal$setLevel(entity.level());
            ((IBlockPos) blockPos).worldportal$setPortalEntrancePos(blockPos2);
            ((IBlockPos) blockPos).worldportal$setPortal(entityX.worldportal$getPortalId());
        }

        return original.call(instance, blockPos, isNether, worldBorder);
    }

    @WrapOperation(
            method = "getExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalForcer;createPortal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;)Ljava/util/Optional;"
            )
    )
    private Optional<BlockUtil.FoundRectangle> getExitPortalForcerCreatePrepare(
            PortalForcer instance,
            BlockPos blockPos,
            Direction.Axis axis,
            Operation<Optional<BlockUtil.FoundRectangle>> original,
            ServerLevel serverLevel,
            Entity entity,
            BlockPos blockPos2
    ) {
        IEntity entityX = (IEntity) entity;

        PortalConfig portalConfig = entityX.worldportal$getPortalConfig();
        if (portalConfig != null) {
            ((IBlockPos) blockPos).worldportal$setLevel(entity.level());
            ((IBlockPos) blockPos).worldportal$setPortalEntrancePos(blockPos2);
            ((IBlockPos) blockPos).worldportal$setPortal(entityX.worldportal$getPortalId());
        }

        return original.call(instance, blockPos, axis);
    }
}
