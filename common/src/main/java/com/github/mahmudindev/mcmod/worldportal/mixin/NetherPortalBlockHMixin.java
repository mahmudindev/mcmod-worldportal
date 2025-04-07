package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.base.IPortalForcer;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
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

@Mixin(value = NetherPortalBlock.class, priority = 1500)
public abstract class NetherPortalBlockHMixin {
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
            Entity entity
    ) {
        PortalData portal = ((IEntity) entity).worldportal$getPortal();
        if (portal != null) {
            IPortalForcer portalForcerX = (IPortalForcer) instance;

            BlockPos blockPosX = portalForcerX.worldportal$setPortal(
                    blockPos,
                    ((IEntity) entity).worldportal$getPortalId()
            );

            Optional<BlockPos> optional = original.call(
                    instance,
                    blockPosX,
                    isNether,
                    worldBorder
            );

            portalForcerX.worldportal$removePortal(blockPosX);

            return optional;
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
            Entity entity
    ) {
        PortalData portal = ((IEntity) entity).worldportal$getPortal();
        if (portal != null) {
            IPortalForcer portalForcerX = (IPortalForcer) instance;

            BlockPos blockPosX = portalForcerX.worldportal$setPortal(
                    blockPos,
                    ((IEntity) entity).worldportal$getPortalId()
            );

            Optional<BlockUtil.FoundRectangle> optional = original.call(
                    instance,
                    blockPosX,
                    axis
            );

            portalForcerX.worldportal$removePortal(blockPosX);

            return optional;
        }

        return original.call(instance, blockPos, axis);
    }
}
