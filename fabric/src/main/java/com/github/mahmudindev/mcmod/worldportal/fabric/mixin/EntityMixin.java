package com.github.mahmudindev.mcmod.worldportal.fabric.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalReturns;
import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.PortalInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntity {
    @Shadow public abstract Level level();

    @WrapOperation(
            method = "changeDimension",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;findDimensionEntryPoint(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/level/portal/PortalInfo;"
            )
    )
    private PortalInfo changeDimensionFindDimensionEntryPointFinish(
            Entity instance,
            ServerLevel serverLevel,
            Operation<PortalInfo> original
    ) {
        PortalInfo portalInfo = original.call(instance, serverLevel);

        if (portalInfo != null) {
            PortalData portal = this.worldportal$getPortal();
            if (portal == null) {
                return portalInfo;
            }

            ResourceKey<Level> dimension = serverLevel.dimension();
            if (dimension != portal.getDestinationKey()) {
                return portalInfo;
            }

            BlockPos blockPos = BlockPos.containing(portalInfo.pos);

            BlockState blockState = serverLevel.getBlockState(blockPos);
            if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                return portalInfo;
            }

            Direction.Axis axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
            BlockUtil.FoundRectangle portalRectangle = BlockUtil.getLargestRectangleAround(
                    blockPos,
                    axis,
                    21,
                    Direction.Axis.Y,
                    21,
                    blockPosX -> serverLevel.getBlockState(blockPosX) == blockState
            );

            IServerLevel serverLevelX = (IServerLevel) serverLevel;

            PortalReturns portalReturns = serverLevelX.worldportal$getPortalReturns();
            portalReturns.putDimension(
                    portalRectangle.minCorner,
                    this.level().dimension()
            );
        }

        return portalInfo;
    }
}
