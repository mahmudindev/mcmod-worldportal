package com.github.mahmudindev.minecraft.worldportal.mixin;

import com.github.mahmudindev.minecraft.worldportal.base.IEntity;
import com.github.mahmudindev.minecraft.worldportal.base.IServerLevel;
import com.github.mahmudindev.minecraft.worldportal.portal.PortalData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = ServerPlayer.class, priority = 1500)
public abstract class ServerPlayerHMixin {
    @WrapOperation(
            method = "getExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalForcer;createPortal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;)Ljava/util/Optional;"
            )
    )
    private Optional<BlockUtil.FoundRectangle> getExitPortalForcerPrepare(
            PortalForcer instance,
            BlockPos blockPos,
            Direction.Axis axis,
            Operation<Optional<BlockUtil.FoundRectangle>> original,
            ServerLevel serverLevel
    ) {
        PortalData portalData = ((IEntity) this).worldportal$getPortalEntranceData();
        if (portalData != null) {
            IServerLevel serverLevelX = (IServerLevel) serverLevel;

            BlockPos blockPosX = serverLevelX.worldportal$setPortalInfo(
                    serverLevel.dimension(),
                    blockPos,
                    portalData
            );

            Optional<BlockUtil.FoundRectangle> portalRectangle = original.call(
                    instance,
                    blockPosX,
                    axis
            );

            serverLevelX.worldportal$removePortalInfo(blockPosX);

            return portalRectangle;
        }

        return original.call(instance, blockPos, axis);
    }
}
