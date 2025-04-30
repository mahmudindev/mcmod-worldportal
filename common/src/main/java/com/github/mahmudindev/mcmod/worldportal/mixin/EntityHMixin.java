package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.base.IPortalForcer;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(value = Entity.class, priority = 1500)
public abstract class EntityHMixin {
    @WrapOperation(
            method = "getExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalForcer;findPortalAround(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Ljava/util/Optional;"
            )
    )
    private Optional<BlockUtil.FoundRectangle> getExitPortalForcerFindPrepare(
            PortalForcer instance,
            BlockPos blockPos,
            boolean isNether,
            WorldBorder worldBorder,
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
                    isNether,
                    worldBorder
            );

            portalForcerX.worldportal$removePortal(blockPosX);

            return portalRectangle;
        }

        return original.call(instance, blockPos, isNether, worldBorder);
    }
}
