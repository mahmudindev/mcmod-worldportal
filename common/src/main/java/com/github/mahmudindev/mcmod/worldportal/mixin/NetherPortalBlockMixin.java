package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IBlockPos;
import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalConfig;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.Identifier;
import net.minecraft.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;

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
            BlockState state,
            ServerLevel level
    ) {
        boolean hasHA = state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS);
        Direction.Axis axis = hasHA
                ? state.getValue(BlockStateProperties.HORIZONTAL_AXIS)
                : Direction.Axis.X;
        BlockUtil.FoundRectangle largestRectangleAround = BlockUtil.getLargestRectangleAround(
                blockPos.above(),
                axis,
                21,
                hasHA ? Direction.Axis.Y : Direction.Axis.X,
                21,
                blockPosX -> level.getBlockState(blockPosX) == state
        );

        BlockPos minCornerPos = largestRectangleAround.minCorner;

        Identifier frameC1 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? largestRectangleAround.axis1Size : 0,
                        hasHA ? -1 : 0,
                        hasHA ? axis == Direction.Axis.Z ? largestRectangleAround.axis1Size : 0 : -1
                )).getBlock()
        );
        Identifier frameC2 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        hasHA ? -1 : 0,
                        hasHA && axis != Direction.Axis.Z ? 0 : -1
                )).getBlock()
        );
        Identifier frameC3 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? largestRectangleAround.axis1Size : 0,
                        hasHA ? largestRectangleAround.axis2Size : 0,
                        hasHA ? axis == Direction.Axis.Z
                                ? largestRectangleAround.axis1Size
                                : 0 : largestRectangleAround.axis2Size
                )).getBlock()
        );
        Identifier frameC4 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        hasHA ? largestRectangleAround.axis2Size : 0,
                        hasHA ? axis == Direction.Axis.Z ? -1 : 0 : largestRectangleAround.axis2Size
                )).getBlock()
        );

        boolean isWorldPortal = false;

        Map<Identifier, PortalConfig> portalConfigs = PortalManager.getPortalConfigs();
        for (Map.Entry<Identifier, PortalConfig> entry : portalConfigs.entrySet()) {
            PortalConfig portalConfig = entry.getValue();

            Identifier mode = portalConfig.getModeLocation();
            if (mode != null && !mode.equals(PortalConfig.DEFAULT_MODE)) {
                continue;
            }

            Identifier frameC1Target = portalConfig.getFrameC1(Direction.Axis.X);
            if (frameC1Target != null && !frameC1.equals(frameC1Target)) {
                continue;
            }

            Identifier frameC2Target = portalConfig.getFrameC2(Direction.Axis.X);
            if (frameC2Target != null && !frameC2.equals(frameC2Target)) {
                continue;
            }

            Identifier frameC3Target = portalConfig.getFrameC3(Direction.Axis.X);
            if (frameC3Target != null && !frameC3.equals(frameC3Target)) {
                continue;
            }

            Identifier frameC4Target = portalConfig.getFrameC4(Direction.Axis.X);
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

    @ModifyArg(
            method = "getExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalForcer;findClosestPortalPosition(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Ljava/util/Optional;"
            ),
            index = 0
    )
    private BlockPos getExitPortalForcerFindPrepare(
            BlockPos approximateExitPos,
            @Local(argsOnly = true) Entity entity,
            @Local(argsOnly = true, ordinal = 0) BlockPos portalEntryPos
    ) {
        IEntity entityX = (IEntity) entity;

        PortalConfig portalConfig = entityX.worldportal$getPortalConfig();
        if (portalConfig != null) {
            ((IBlockPos) approximateExitPos).worldportal$setLevel(entity.level());
            ((IBlockPos) approximateExitPos).worldportal$setPortalEntrancePos(portalEntryPos);
            ((IBlockPos) approximateExitPos).worldportal$setPortal(entityX.worldportal$getPortalId());
        }

        return approximateExitPos;
    }

    @ModifyArg(
            method = "getExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalForcer;createPortal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;)Ljava/util/Optional;"
            ),
            index = 0
    )
    private BlockPos getExitPortalForcerCreatePrepare(
            BlockPos origin,
            @Local(argsOnly = true) Entity entity,
            @Local(argsOnly = true, ordinal = 0) BlockPos portalEntryPos
    ) {
        IEntity entityX = (IEntity) entity;

        PortalConfig portalConfig = entityX.worldportal$getPortalConfig();
        if (portalConfig != null) {
            ((IBlockPos) origin).worldportal$setLevel(entity.level());
            ((IBlockPos) origin).worldportal$setPortalEntrancePos(portalEntryPos);
            ((IBlockPos) origin).worldportal$setPortal(entityX.worldportal$getPortalId());
        }

        return origin;
    }
}
