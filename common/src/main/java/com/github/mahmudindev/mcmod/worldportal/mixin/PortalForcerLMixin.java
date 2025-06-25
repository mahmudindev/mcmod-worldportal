package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IBlockPos;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(value = PortalForcer.class, priority = 750)
public abstract class PortalForcerLMixin {
    @Shadow @Final private ServerLevel level;

    @WrapOperation(
            method = "findClosestPortalPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;getInSquare(Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/entity/ai/village/poi/PoiManager$Occupancy;)Ljava/util/stream/Stream;"
            )
    )
    private Stream<PoiRecord> findPortalAroundPoiManagerGetInSquareFilter(
            PoiManager instance,
            Predicate<Holder<PoiType>> predicate,
            BlockPos blockPos,
            int distance,
            PoiManager.Occupancy occupancy,
            Operation<Stream<PoiRecord>> original
    ) {
        Stream<PoiRecord> poiRecordStream = original.call(
                instance,
                predicate,
                blockPos,
                distance,
                occupancy
        );

        PortalData portal = ((IBlockPos) blockPos).worldportal$getPortal();
        Map<BlockPos, Boolean> blockPosPassMap = new HashMap<>();

        return poiRecordStream.filter(poiRecord -> {
            BlockPos blockPosX = poiRecord.getPos();

            Boolean blockPosPass = blockPosPassMap.get(blockPosX);
            if (blockPosPass != null) {
                return blockPosPass;
            }

            BlockState blockState = this.level.getBlockState(blockPosX);
            if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                return false;
            }

            Direction.Axis axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
            BlockUtil.FoundRectangle foundRectangle = BlockUtil.getLargestRectangleAround(
                    blockPosX,
                    axis,
                    21,
                    Direction.Axis.Y,
                    21,
                    blockPosZ -> this.level.getBlockState(blockPosZ) == blockState
            );

            ResourceLocation frameC1 = BuiltInRegistries.BLOCK.getKey(
                    this.level.getBlockState(foundRectangle.minCorner.offset(
                            axis == Direction.Axis.X ? -1 : 0,
                            -1,
                            axis == Direction.Axis.Z ? -1 : 0
                    )).getBlock()
            );
            ResourceLocation frameC2 = BuiltInRegistries.BLOCK.getKey(
                    this.level.getBlockState(foundRectangle.minCorner.offset(
                            axis == Direction.Axis.X ? foundRectangle.axis1Size : 0,
                            -1,
                            axis == Direction.Axis.Z ? foundRectangle.axis1Size : 0
                    )).getBlock()
            );
            ResourceLocation frameC3 = BuiltInRegistries.BLOCK.getKey(
                    this.level.getBlockState(foundRectangle.minCorner.offset(
                            axis == Direction.Axis.X ? -1 : 0,
                            foundRectangle.axis2Size,
                            axis == Direction.Axis.Z ? -1 : 0
                    )).getBlock()
            );
            ResourceLocation frameC4 = BuiltInRegistries.BLOCK.getKey(
                    this.level.getBlockState(foundRectangle.minCorner.offset(
                            axis == Direction.Axis.X ? foundRectangle.axis1Size : 0,
                            foundRectangle.axis2Size,
                            axis == Direction.Axis.Z ? foundRectangle.axis1Size : 0
                    )).getBlock()
            );

            boolean blockPosPassX = true;

            if (portal != null) {
                for (ResourceLocation[] v : new ResourceLocation[][]{
                        {frameC1, portal.getFrameBottomLeftLocation()},
                        {frameC2, portal.getFrameBottomRightLocation()},
                        {frameC3, portal.getFrameTopLeftLocation()},
                        {frameC4, portal.getFrameTopRightLocation()}
                }) {
                    if (v[0].equals(v[1])) {
                        continue;
                    }

                    blockPosPassX = false;
                    break;
                }
            } else {
                Map<ResourceLocation, PortalData> portals = PortalManager.getPortals();
                for (Map.Entry<ResourceLocation, PortalData> entry : portals.entrySet()) {
                    PortalData portalX = entry.getValue();

                    ResourceLocation c1 = portalX.getFrameBottomLeftLocation();
                    if (c1 != null && !c1.equals(frameC1)) {
                        continue;
                    }

                    ResourceLocation c2 = portalX.getFrameBottomRightLocation();
                    if (c2 != null && !c2.equals(frameC2)) {
                        continue;
                    }

                    ResourceLocation c3 = portalX.getFrameTopLeftLocation();
                    if (c3 != null && !c3.equals(frameC3)) {
                        continue;
                    }

                    ResourceLocation c4 = portalX.getFrameTopRightLocation();
                    if (c4 != null && !c4.equals(frameC4)) {
                        continue;
                    }

                    blockPosPassX = false;
                    break;
                }
            }

            for (int i = 0; i < foundRectangle.axis1Size; i++) {
                for (int j = 0; j < foundRectangle.axis2Size; j++) {
                    blockPosPassMap.put(foundRectangle.minCorner.offset(
                            axis == Direction.Axis.X ? i : 0,
                            j,
                            axis == Direction.Axis.Z ? i : 0
                    ), blockPosPassX);
                }
            }

            return blockPosPassX;
        });
    }

    @WrapMethod(method = "createPortal")
    private Optional<BlockUtil.FoundRectangle> createPortalProcess(
            BlockPos blockPos,
            Direction.Axis axis,
            Operation<Optional<BlockUtil.FoundRectangle>> original
    ) {
        Optional<BlockUtil.FoundRectangle> optional = original.call(blockPos, axis);
        if (optional.isEmpty()) {
            return optional;
        }

        PortalData portal = ((IBlockPos) blockPos).worldportal$getPortal();
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
