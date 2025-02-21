package com.github.mahmudindev.minecraft.worldportal.mixin;

import com.github.mahmudindev.minecraft.worldportal.WorldPortal;
import com.github.mahmudindev.minecraft.worldportal.base.IServerLevel;
import com.github.mahmudindev.minecraft.worldportal.portal.PortalData;
import com.github.mahmudindev.minecraft.worldportal.portal.PortalManager;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(PortalForcer.class)
public abstract class PortalForcerMixin {
    @Shadow @Final private ServerLevel level;

    @WrapOperation(
            method = "findPortalAround",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;getInSquare(Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/entity/ai/village/poi/PoiManager$Occupancy;)Ljava/util/stream/Stream;"
            )
    )
    private Stream<PoiRecord> findPortalAroundPoiManagerGetInSquareFilter(
            PoiManager instance,
            Predicate<Holder<PoiType>> holderPredicate,
            BlockPos blockPos,
            int distance,
            PoiManager.Occupancy occupancy,
            Operation<Stream<PoiRecord>> original,
            @Share(
                    namespace = WorldPortal.MOD_ID + "_PortalForcer",
                    value = "virtualPos"
            ) LocalRef<BlockPos> virtualPosRef
    ) {
        Stream<PoiRecord> poiRecordStream = original.call(
                instance,
                holderPredicate,
                blockPos,
                distance,
                occupancy
        );

        IServerLevel serverLevelX = (IServerLevel) this.level;

        PortalData portalData = serverLevelX.worldportal$getPortalInfoData(virtualPosRef.get());
        if (portalData != null) {
            List<BlockUtil.FoundRectangle> portalRectangles = new LinkedList<>();
            Map<BlockPos, Boolean> portalCornerPasses = new HashMap<>();

            return poiRecordStream.filter(poiRecord -> {
                BlockPos blockPosZ = poiRecord.getPos();

                BlockUtil.FoundRectangle portalRectangle = null;
                Direction.Axis axis = null;

                for (BlockUtil.FoundRectangle portalRectangleX : portalRectangles) {
                    BlockPos minCornerPos = portalRectangleX.minCorner;

                    BlockState blockState = this.level.getBlockState(minCornerPos);
                    if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                        continue;
                    }

                    axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                    if (axis == Direction.Axis.X) {
                        if (blockPosZ.getX() < minCornerPos.getX()) {
                            continue;
                        }

                        int axisSize = portalRectangleX.axis1Size;
                        if (blockPosZ.getX() > minCornerPos.getX() + axisSize) {
                            continue;
                        }
                    } else {
                        if (blockPosZ.getZ() < minCornerPos.getZ()) {
                            continue;
                        }

                        int axisSize = portalRectangleX.axis1Size;
                        if (blockPosZ.getZ() > minCornerPos.getZ() + axisSize - 1) {
                            continue;
                        }
                    }

                    if (blockPosZ.getY() < minCornerPos.getY()) {
                        continue;
                    }

                    int axisSize = portalRectangleX.axis2Size;
                    if (blockPosZ.getY() > minCornerPos.getY() + axisSize - 1) {
                        continue;
                    }

                    portalRectangle = portalRectangleX;
                }

                if (portalRectangle == null) {
                    BlockState blockState = this.level.getBlockState(blockPosZ);
                    if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                        return false;
                    }

                    axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                    portalRectangle = PortalManager.getPortalRectangle(
                            this.level,
                            blockPosZ,
                            blockState,
                            axis
                    );

                    portalRectangles.add(portalRectangle);
                }

                Boolean portalCornerPass = portalCornerPasses.get(portalRectangle.minCorner);
                if (portalCornerPass != null) {
                    return portalCornerPass;
                }

                portalCornerPasses.put(portalRectangle.minCorner, false);

                ResourceLocation frameC1 = portalData.getFrameBottomLeftLocation();
                if (frameC1 != null && !BuiltInRegistries.BLOCK.getKey(
                        this.level.getBlockState(portalRectangle.minCorner.offset(
                                axis == Direction.Axis.X ? -1 : 0,
                                -1,
                                axis == Direction.Axis.Z ? -1 : 0
                        )).getBlock()
                ).equals(frameC1)) {
                    return false;
                }
                ResourceLocation frameC2 = portalData.getFrameBottomRightLocation();
                if (frameC2 != null && !BuiltInRegistries.BLOCK.getKey(
                        this.level.getBlockState(portalRectangle.minCorner.offset(
                                axis == Direction.Axis.X ? portalRectangle.axis1Size : 0,
                                -1,
                                axis == Direction.Axis.Z ? portalRectangle.axis1Size : 0
                        )).getBlock()
                ).equals(frameC2)) {
                    return false;
                }
                ResourceLocation frameC3 = portalData.getFrameTopLeftLocation();
                if (frameC3 != null && !BuiltInRegistries.BLOCK.getKey(
                        this.level.getBlockState(portalRectangle.minCorner.offset(
                                axis == Direction.Axis.X ? -1 : 0,
                                portalRectangle.axis2Size,
                                axis == Direction.Axis.Z ? -1 : 0
                        )).getBlock()
                ).equals(frameC3)) {
                    return false;
                }
                ResourceLocation frameC4 = portalData.getFrameTopRightLocation();
                if (frameC4 != null && !BuiltInRegistries.BLOCK.getKey(
                        this.level.getBlockState(portalRectangle.minCorner.offset(
                                axis == Direction.Axis.X ? portalRectangle.axis1Size : 0,
                                portalRectangle.axis2Size,
                                axis == Direction.Axis.Z ? portalRectangle.axis1Size : 0
                        )).getBlock()
                ).equals(frameC4)) {
                    return false;
                }

                portalCornerPasses.put(portalRectangle.minCorner, true);

                return true;
            });
        }

        return poiRecordStream;
    }

    @WrapMethod(method = "createPortal")
    private Optional<BlockUtil.FoundRectangle> createPortalBuild(
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

        IServerLevel serverLevelX = (IServerLevel) this.level;

        PortalData portalData = serverLevelX.worldportal$getPortalInfoData(virtualPosRef.get());
        if (portalData != null) {
            BlockUtil.FoundRectangle portalRectangle = optional.get();

            ResourceLocation frameC1 = portalData.getFrameBottomLeftLocation();
            if (frameC1 != null) {
                Block block = BuiltInRegistries.BLOCK.get(frameC1);
                this.level.setBlockAndUpdate(portalRectangle.minCorner.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        -1,
                        axis == Direction.Axis.Z ? -1 : 0
                ), block.defaultBlockState());
            }
            ResourceLocation frameC2 = portalData.getFrameBottomRightLocation();
            if (frameC2 != null) {
                Block block = BuiltInRegistries.BLOCK.get(frameC2);
                this.level.setBlockAndUpdate(portalRectangle.minCorner.offset(
                        axis == Direction.Axis.X ? portalRectangle.axis1Size : 0,
                        -1,
                        axis == Direction.Axis.Z ? portalRectangle.axis1Size : 0
                ), block.defaultBlockState());
            }
            ResourceLocation frameC3 = portalData.getFrameTopLeftLocation();
            if (frameC3 != null) {
                Block block = BuiltInRegistries.BLOCK.get(frameC3);
                this.level.setBlockAndUpdate(portalRectangle.minCorner.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        portalRectangle.axis2Size,
                        axis == Direction.Axis.Z ? -1 : 0
                ), block.defaultBlockState());
            }
            ResourceLocation frameC4 = portalData.getFrameTopRightLocation();
            if (frameC4 != null) {
                Block block = BuiltInRegistries.BLOCK.get(frameC4);
                this.level.setBlockAndUpdate(portalRectangle.minCorner.offset(
                        axis == Direction.Axis.X ? portalRectangle.axis1Size : 0,
                        portalRectangle.axis2Size,
                        axis == Direction.Axis.Z ? portalRectangle.axis1Size : 0
                ), block.defaultBlockState());
            }
        }

        return optional;
    }
}
