package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IBlockPos;
import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalConfig;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Optional;

@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {
    @WrapOperation(
            method = "randomTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;isValidSpawn(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntityType;)Z"
            )
    )
    private boolean randomTickIsValidSpawn(
            BlockState instance,
            BlockGetter blockGetter,
            BlockPos blockPos,
            EntityType<?> entityType,
            Operation<Boolean> original,
            BlockState blockState,
            ServerLevel serverLevel
    ) {
        boolean hasHA = blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS);
        Direction.Axis axis = hasHA
                ? blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS)
                : Direction.Axis.X;
        BlockUtil.FoundRectangle foundRectangle = BlockUtil.getLargestRectangleAround(
                blockPos.above(),
                axis,
                21,
                hasHA ? Direction.Axis.Y : Direction.Axis.Z,
                21,
                blockPosX -> serverLevel.getBlockState(blockPosX) == blockState
        );

        BlockPos minCornerPos = foundRectangle.minCorner;

        ResourceLocation frameC1 = BuiltInRegistries.BLOCK.getKey(
                serverLevel.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? foundRectangle.axis1Size : 0,
                        hasHA ? -1 : 0,
                        hasHA ? axis == Direction.Axis.Z ? foundRectangle.axis1Size : 0 : -1
                )).getBlock()
        );
        ResourceLocation frameC2 = BuiltInRegistries.BLOCK.getKey(
                serverLevel.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        hasHA ? -1 : 0,
                        hasHA && axis != Direction.Axis.Z ? 0 : -1
                )).getBlock()
        );
        ResourceLocation frameC3 = BuiltInRegistries.BLOCK.getKey(
                serverLevel.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? foundRectangle.axis1Size : 0,
                        hasHA ? foundRectangle.axis2Size : 0,
                        hasHA ? axis == Direction.Axis.Z
                                ? foundRectangle.axis1Size
                                : 0 : foundRectangle.axis2Size
                )).getBlock()
        );
        ResourceLocation frameC4 = BuiltInRegistries.BLOCK.getKey(
                serverLevel.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        hasHA ? foundRectangle.axis2Size : 0,
                        hasHA ? axis == Direction.Axis.Z ? -1 : 0 : foundRectangle.axis2Size
                )).getBlock()
        );

        boolean isWorldPortal = false;

        Map<ResourceLocation, PortalConfig> portalConfigs = PortalManager.getPortalConfigs();
        for (Map.Entry<ResourceLocation, PortalConfig> entry : portalConfigs.entrySet()) {
            PortalConfig portalConfig = entry.getValue();

            ResourceLocation mode = portalConfig.getModeLocation();
            if (mode != null && !mode.equals(PortalConfig.DEFAULT_MODE)) {
                continue;
            }

            ResourceLocation frameC1Target = portalConfig.getFrameC1(Direction.Axis.X);
            if (frameC1Target != null && !frameC1.equals(frameC1Target)) {
                continue;
            }

            ResourceLocation frameC2Target = portalConfig.getFrameC2(Direction.Axis.X);
            if (frameC2Target != null && !frameC2.equals(frameC2Target)) {
                continue;
            }

            ResourceLocation frameC3Target = portalConfig.getFrameC3(Direction.Axis.X);
            if (frameC3Target != null && !frameC3.equals(frameC3Target)) {
                continue;
            }

            ResourceLocation frameC4Target = portalConfig.getFrameC4(Direction.Axis.X);
            if (frameC4Target != null && !frameC4.equals(frameC4Target)) {
                continue;
            }

            isWorldPortal = true;
            break;
        }

        if (isWorldPortal) {
            return false;
        }

        return original.call(instance, blockGetter, blockPos, entityType);
    }

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
