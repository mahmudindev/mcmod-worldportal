package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.github.mahmudindev.mcmod.worldportal.base.IPortalForcer;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(PortalForcer.class)
public abstract class PortalForcerMixin implements IPortalForcer {
    @Shadow @Final private ServerLevel level;

    @WrapMethod(method = "findClosestPortalPosition")
    private Optional<BlockPos> findPortalAroundProcess(
            BlockPos blockPos,
            boolean isNether,
            WorldBorder worldBorder,
            Operation<Optional<BlockPos>> original,
            @Share(
                    namespace = WorldPortal.MOD_ID + "_PortalForcer",
                    value = "virtualPos"
            ) LocalRef<BlockPos> virtualPosRef
    ) {
        PortalData portal = this.worldportal$getPortal(virtualPosRef.get());
        if (portal != null) {
            int searchRadius = isNether ? 16 : 128;

            PoiManager poiManager = this.level.getPoiManager();
            poiManager.ensureLoadedAndValid(this.level, blockPos, searchRadius);

            List<BlockUtil.FoundRectangle> foundRectangles = new LinkedList<>();
            Map<BlockPos, Boolean> cornerMap = new HashMap<>();

            return poiManager.getInSquare(
                    holder -> holder.is(PoiTypes.NETHER_PORTAL),
                    blockPos,
                    searchRadius,
                    PoiManager.Occupancy.ANY
            ).map(PoiRecord::getPos).filter(worldBorder::isWithinBounds).filter(v -> {
                BlockUtil.FoundRectangle foundRectangle = null;
                Direction.Axis axis = null;

                for (BlockUtil.FoundRectangle foundRectangleX : foundRectangles) {
                    BlockPos cornerPos = foundRectangleX.minCorner;

                    BlockState blockState = this.level.getBlockState(cornerPos);
                    if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                        continue;
                    }

                    axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                    if (axis == Direction.Axis.X) {
                        if (v.getX() < cornerPos.getX()) {
                            continue;
                        }

                        int axisSize = foundRectangleX.axis1Size;
                        if (v.getX() > cornerPos.getX() + axisSize) {
                            continue;
                        }
                    } else {
                        if (v.getZ() < cornerPos.getZ()) {
                            continue;
                        }

                        int axisSize = foundRectangleX.axis1Size;
                        if (v.getZ() > cornerPos.getZ() + axisSize - 1) {
                            continue;
                        }
                    }

                    if (v.getY() < cornerPos.getY()) {
                        continue;
                    }

                    int axisSize = foundRectangleX.axis2Size;
                    if (v.getY() > cornerPos.getY() + axisSize - 1) {
                        continue;
                    }

                    foundRectangle = foundRectangleX;
                }

                if (foundRectangle == null) {
                    BlockState blockState = this.level.getBlockState(v);
                    if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                        return false;
                    }

                    axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                    foundRectangle = BlockUtil.getLargestRectangleAround(
                            v,
                            axis,
                            21,
                            Direction.Axis.Y,
                            21,
                            blockPosX -> this.level.getBlockState(blockPosX) == blockState
                    );

                    foundRectangles.add(foundRectangle);
                }

                Boolean cornerPass = cornerMap.get(foundRectangle.minCorner);
                if (cornerPass != null) {
                    return cornerPass;
                }

                cornerMap.put(foundRectangle.minCorner, false);

                ResourceLocation frameC1 = portal.getFrameBottomLeftLocation();
                if (frameC1 != null && !BuiltInRegistries.BLOCK.getKey(
                        this.level.getBlockState(foundRectangle.minCorner.offset(
                                axis == Direction.Axis.X ? -1 : 0,
                                -1,
                                axis == Direction.Axis.Z ? -1 : 0
                        )).getBlock()
                ).equals(frameC1)) {
                    return false;
                }
                ResourceLocation frameC2 = portal.getFrameBottomRightLocation();
                if (frameC2 != null && !BuiltInRegistries.BLOCK.getKey(
                        this.level.getBlockState(foundRectangle.minCorner.offset(
                                axis == Direction.Axis.X ? foundRectangle.axis1Size : 0,
                                -1,
                                axis == Direction.Axis.Z ? foundRectangle.axis1Size : 0
                        )).getBlock()
                ).equals(frameC2)) {
                    return false;
                }
                ResourceLocation frameC3 = portal.getFrameTopLeftLocation();
                if (frameC3 != null && !BuiltInRegistries.BLOCK.getKey(
                        this.level.getBlockState(foundRectangle.minCorner.offset(
                                axis == Direction.Axis.X ? -1 : 0,
                                foundRectangle.axis2Size,
                                axis == Direction.Axis.Z ? -1 : 0
                        )).getBlock()
                ).equals(frameC3)) {
                    return false;
                }
                ResourceLocation frameC4 = portal.getFrameTopRightLocation();
                if (frameC4 != null && !BuiltInRegistries.BLOCK.getKey(
                        this.level.getBlockState(foundRectangle.minCorner.offset(
                                axis == Direction.Axis.X ? foundRectangle.axis1Size : 0,
                                foundRectangle.axis2Size,
                                axis == Direction.Axis.Z ? foundRectangle.axis1Size : 0
                        )).getBlock()
                ).equals(frameC4)) {
                    return false;
                }

                cornerMap.put(foundRectangle.minCorner, true);

                return true;
            }).min(Comparator.comparingDouble((BlockPos blockPosX) -> {
                return blockPosX.distSqr(blockPos);
            }).thenComparingInt(Vec3i::getY));
        }

        return original.call(blockPos, isNether, worldBorder);
    }

    @WrapMethod(method = "createPortal")
    private Optional<BlockUtil.FoundRectangle> createPortalProcess(
            BlockPos blockPos,
            Direction.Axis axis,
            Operation<Optional<BlockUtil.FoundRectangle>> original,
            @Share(
                    namespace = WorldPortal.MOD_ID + "_PortalForcer",
                    value = "virtualPos"
            ) LocalRef<BlockPos> virtualPosRef
    ) {
        Optional<BlockUtil.FoundRectangle> optional = original.call(blockPos, axis);
        if (optional.isEmpty()) {
            return optional;
        }

        PortalData portal = this.worldportal$getPortal(virtualPosRef.get());
        if (portal != null) {
            BlockUtil.FoundRectangle foundRectangle = optional.get();

            ResourceLocation frameC1 = portal.getFrameBottomLeftLocation();
            if (frameC1 != null) {
                Block block = BuiltInRegistries.BLOCK.get(frameC1);
                this.level.setBlockAndUpdate(foundRectangle.minCorner.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        -1,
                        axis == Direction.Axis.Z ? -1 : 0
                ), block.defaultBlockState());
            }
            ResourceLocation frameC2 = portal.getFrameBottomRightLocation();
            if (frameC2 != null) {
                Block block = BuiltInRegistries.BLOCK.get(frameC2);
                this.level.setBlockAndUpdate(foundRectangle.minCorner.offset(
                        axis == Direction.Axis.X ? foundRectangle.axis1Size : 0,
                        -1,
                        axis == Direction.Axis.Z ? foundRectangle.axis1Size : 0
                ), block.defaultBlockState());
            }
            ResourceLocation frameC3 = portal.getFrameTopLeftLocation();
            if (frameC3 != null) {
                Block block = BuiltInRegistries.BLOCK.get(frameC3);
                this.level.setBlockAndUpdate(foundRectangle.minCorner.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        foundRectangle.axis2Size,
                        axis == Direction.Axis.Z ? -1 : 0
                ), block.defaultBlockState());
            }
            ResourceLocation frameC4 = portal.getFrameTopRightLocation();
            if (frameC4 != null) {
                Block block = BuiltInRegistries.BLOCK.get(frameC4);
                this.level.setBlockAndUpdate(foundRectangle.minCorner.offset(
                        axis == Direction.Axis.X ? foundRectangle.axis1Size : 0,
                        foundRectangle.axis2Size,
                        axis == Direction.Axis.Z ? foundRectangle.axis1Size : 0
                ), block.defaultBlockState());
            }
        }

        return optional;
    }
}
