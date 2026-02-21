package com.github.mahmudindev.mcmod.worldportal.forge.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalConfig;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements IEntity {
    @Shadow public abstract ServerLevel serverLevel();

    @WrapOperation(
            method = "changeDimension",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/util/ITeleporter;getPortalInfo(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Function;)Lnet/minecraft/world/level/portal/PortalInfo;"
            ),
            remap = false
    )
    private PortalInfo changeDimensionFindDimensionEntryPointFinish(
            ITeleporter instance,
            Entity entity,
            ServerLevel serverLevel,
            Function<ServerLevel, PortalInfo> defaultPortalInfo,
            Operation<PortalInfo> original
    ) {
        PortalInfo portalInfo = original.call(
                instance,
                entity,
                serverLevel,
                defaultPortalInfo
        );

        if (portalInfo != null) {
            PortalConfig portalConfig = this.worldportal$getPortalConfig();
            if (portalConfig == null) {
                return portalInfo;
            }

            ResourceKey<Level> dimension = serverLevel.dimension();
            if (dimension != portalConfig.getDestinationKey()) {
                return portalInfo;
            }

            BlockPos blockPos = BlockPos.containing(portalInfo.pos);

            BlockState blockState = serverLevel.getBlockState(blockPos);
            boolean hasHA = blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS);
            Direction.Axis axis = hasHA
                    ? blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS)
                    : Direction.Axis.X;
            BlockUtil.FoundRectangle portalRectangle = BlockUtil.getLargestRectangleAround(
                    blockPos,
                    axis,
                    21,
                    hasHA ? Direction.Axis.Y : Direction.Axis.X,
                    21,
                    blockPosX -> serverLevel.getBlockState(blockPosX) == blockState
            );

            IServerLevel serverLevelX = (IServerLevel) serverLevel;

            PortalData portalData = serverLevelX.worldportal$getPortalData();

            ResourceKey<Block> resourceKey = ResourceKey.create(
                    Registries.BLOCK,
                    BuiltInRegistries.BLOCK.getKey(blockState.getBlock())
            );
            for (int i = 0; i < portalRectangle.axis1Size; i++) {
                for (int j = 0; j < portalRectangle.axis2Size; j++) {
                    portalData.putBlock(
                            portalRectangle.minCorner.offset(
                                    axis == Direction.Axis.X ? i : 0,
                                    hasHA ? j : 0,
                                    hasHA ? axis == Direction.Axis.Z ? i : 0 : j
                            ),
                            resourceKey
                    );
                }
            }

            portalData.putDimension(portalRectangle.minCorner, this.serverLevel().dimension());
        }

        return portalInfo;
    }
}
