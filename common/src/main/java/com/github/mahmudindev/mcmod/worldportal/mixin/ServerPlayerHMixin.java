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
    private Optional<BlockUtil.FoundRectangle> getExitPortalForcerCreatePrepare(
            PortalForcer instance,
            BlockPos blockPos,
            Direction.Axis axis,
            Operation<Optional<BlockUtil.FoundRectangle>> original,
            ServerLevel serverLevel
    ) {
        PortalData portal = ((IEntity) this).worldportal$getPortal();
        if (portal != null) {
            IPortalForcer portalForcerX = (IPortalForcer) instance;

            BlockPos blockPosX = portalForcerX.worldportal$setPortal(
                    blockPos,
                    ((IEntity) this).worldportal$getPortalId()
            );

            Optional<BlockUtil.FoundRectangle> portalRectangle = original.call(
                    instance,
                    blockPosX,
                    axis
            );

            portalForcerX.worldportal$removePortal(blockPosX);

            return portalRectangle;
        }

        return original.call(instance, blockPos, axis);
    }
}
