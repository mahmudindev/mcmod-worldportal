package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.github.mahmudindev.mcmod.worldportal.base.IBlockPos;
import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalConfig;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BlockUtil;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
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
    private Stream<PoiRecord> findClosestPortalPositionGetInSquareInclude(
            PoiManager instance,
            Predicate<Holder<PoiType>> predicate,
            BlockPos center,
            int radius,
            PoiManager.Occupancy occupancy,
            Operation<Stream<PoiRecord>> original
    ) {
        Stream<PoiRecord> poiRecordStream = original.call(
                instance,
                predicate,
                center,
                radius,
                occupancy
        );

        IServerLevel serverLevelX = (IServerLevel) this.level;
        PortalData portalData = serverLevelX.worldportal$getPortalData();
        ResourceKey<Block> blockResourceKey = ResourceKey.create(
                Registries.BLOCK,
                BuiltInRegistries.BLOCK.getKey(Blocks.NETHER_PORTAL)
        );
        ResourceKey<PoiType> poiTypeResourceKey = PoiTypes.NETHER_PORTAL;
        PoiType poiType = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValue(poiTypeResourceKey);
        Holder<PoiType> poiTypeHolder = Holder.direct(poiType);
        Stream<PoiRecord> poiRecordStreamX = ChunkPos.rangeClosed(
                new ChunkPos(center.getX(), center.getZ()),
                Math.floorDiv(radius, 16) + 1
        ).flatMap(chunkPos -> IntStream.range(
                this.level.getMinSectionY(),
                this.level.getMaxSectionY()
        ).boxed().flatMap(height -> SectionPos.of(
                chunkPos,
                height
        ).blocksInside()).map(posX -> {
            ResourceKey<Block> blockResourceKeyX = portalData.getBlock(posX);

            if (blockResourceKeyX == null || blockResourceKeyX == blockResourceKey) {
                return null;
            }

            ChunkAccess chunkAccess = this.level.getChunk(posX);
            BlockState state = chunkAccess.getBlockState(posX);
            if (ResourceKey.create(
                    Registries.BLOCK,
                    BuiltInRegistries.BLOCK.getKey(state.getBlock())
            ) != blockResourceKeyX) {
                portalData.removeBlock(posX);

                return null;
            }

            return new PoiRecord(posX, poiTypeHolder, () -> {});
        }).filter(Objects::nonNull)).filter(poiRecord -> {
            BlockPos posX = poiRecord.getPos();
            if (Math.abs(posX.getX() - center.getX()) > radius) {
                return false;
            }

            if (Math.abs(posX.getZ() - center.getZ()) > radius) {
                return false;
            }

            return true;
        });

        return Stream.concat(poiRecordStream, poiRecordStreamX);
    }

    @WrapOperation(
            method = "findClosestPortalPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;",
                    ordinal = 0
            )
    )
    private Stream<BlockPos> findClosestPortalPositionFilter(
            Stream<BlockPos> instance,
            Predicate<? super BlockPos> predicate,
            Operation<Stream<BlockPos>> original,
            BlockPos approximateExitPos
    ) {
        Map<BlockPos, Boolean> posPassMap = new HashMap<>();
        PortalConfig portalConfig = ((IBlockPos) approximateExitPos).worldportal$getPortalConfig();
        Level level = ((IBlockPos) approximateExitPos).worldportal$getLevel();
        boolean hasHA = level == null || level
                .getBlockState(((IBlockPos) approximateExitPos).worldportal$getPortalEntrancePos())
                .hasProperty(BlockStateProperties.HORIZONTAL_AXIS);

        return original.call(instance, predicate).filter(posX -> {

            Boolean posPass = posPassMap.get(posX);
            if (posPass != null) {
                return posPass;
            }

            Direction.Axis AxisX = Direction.Axis.X;
            Direction.Axis AxisY = Direction.Axis.Y;
            Direction.Axis AxisZ = Direction.Axis.Z;

            BlockState state = this.level.getBlockState(posX);
            boolean hasHAX = state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS);
            Direction.Axis axis = hasHAX
                    ? state.getValue(BlockStateProperties.HORIZONTAL_AXIS)
                    : AxisX;
            BlockUtil.FoundRectangle largestRectangleAround = BlockUtil.getLargestRectangleAround(
                    posX,
                    axis,
                    21,
                    hasHAX ? AxisY : AxisZ,
                    21,
                    posZ -> this.level.getBlockState(posZ) == state
            );

            Identifier frameC1 = BuiltInRegistries.BLOCK.getKey(
                    this.level.getBlockState(largestRectangleAround.minCorner.offset(
                            axis == AxisX ? largestRectangleAround.axis1Size : 0,
                            hasHAX ? -1 : 0,
                            hasHAX ? axis == AxisZ ? largestRectangleAround.axis1Size : 0 : -1
                    )).getBlock()
            );
            Identifier frameC2 = BuiltInRegistries.BLOCK.getKey(
                    this.level.getBlockState(largestRectangleAround.minCorner.offset(
                            axis == AxisX ? -1 : 0,
                            hasHAX ? -1 : 0,
                            hasHAX && axis != AxisZ ? 0 : -1
                    )).getBlock()
            );
            Identifier frameC3 = BuiltInRegistries.BLOCK.getKey(
                    this.level.getBlockState(largestRectangleAround.minCorner.offset(
                            axis == AxisX ? largestRectangleAround.axis1Size : 0,
                            hasHAX ? largestRectangleAround.axis2Size : 0,
                            hasHAX ? axis == AxisZ
                                    ? largestRectangleAround.axis1Size
                                    : 0 : largestRectangleAround.axis2Size
                    )).getBlock()
            );
            Identifier frameC4 = BuiltInRegistries.BLOCK.getKey(
                    this.level.getBlockState(largestRectangleAround.minCorner.offset(
                            axis == AxisX ? -1 : 0,
                            hasHAX ? largestRectangleAround.axis2Size : 0,
                            hasHAX ? axis == AxisZ ? -1 : 0 : largestRectangleAround.axis2Size
                    )).getBlock()
            );

            boolean posPassX = false;

            if (portalConfig != null) {
                posPassX = hasHA == hasHAX;

                if (posPassX) {
                    for (Identifier[] v : new Identifier[][]{
                            {frameC1, portalConfig.getFrameBottomLeftLocation()},
                            {frameC2, portalConfig.getFrameBottomRightLocation()},
                            {frameC3, portalConfig.getFrameTopLeftLocation()},
                            {frameC4, portalConfig.getFrameTopRightLocation()}
                    }) {
                        if (v[1] != null && v[0].equals(v[1])) {
                            continue;
                        }

                        posPassX = false;
                        break;
                    }
                }
            } else if (hasHAX) {
                posPassX = true;

                Map<Identifier, PortalConfig> portalConfigs = PortalManager.getPortalConfigs();
                for (Map.Entry<Identifier, PortalConfig> entry : portalConfigs.entrySet()) {
                    PortalConfig portalConfigX = entry.getValue();

                    Identifier mode = portalConfigX.getModeLocation();
                    if (mode != null && !mode.equals(PortalConfig.DEFAULT_MODE)) {
                        continue;
                    }

                    Identifier c1 = portalConfigX.getFrameBottomLeftLocation();
                    if (c1 != null && !c1.equals(frameC1)) {
                        continue;
                    }

                    Identifier c2 = portalConfigX.getFrameBottomRightLocation();
                    if (c2 != null && !c2.equals(frameC2)) {
                        continue;
                    }

                    Identifier c3 = portalConfigX.getFrameTopLeftLocation();
                    if (c3 != null && !c3.equals(frameC3)) {
                        continue;
                    }

                    Identifier c4 = portalConfigX.getFrameTopRightLocation();
                    if (c4 != null && !c4.equals(frameC4)) {
                        continue;
                    }

                    posPassX = false;
                    break;
                }
            }

            for (int i = 0; i < largestRectangleAround.axis1Size; i++) {
                for (int j = 0; j < largestRectangleAround.axis2Size; j++) {
                    posPassMap.put(largestRectangleAround.minCorner.offset(
                            axis == AxisX ? i : 0,
                            hasHAX ? j : 0,
                            hasHAX ? axis == AxisZ ? i : 0 : j
                    ), posPassX);
                }
            }

            return posPassX;
        });
    }

    @WrapOperation(
            method = "lambda$findClosestPortalPosition$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;hasProperty(Lnet/minecraft/world/level/block/state/properties/Property;)Z"
            )
    )
    private boolean method31119HasPropertySkip(
            BlockState instance,
            Property<?> property,
            Operation<Boolean> original
    ) {
        return true;
    }

    @WrapMethod(method = "createPortal")
    private Optional<BlockUtil.FoundRectangle> createPortalProcess(
            BlockPos origin,
            Direction.Axis portalAxis,
            Operation<Optional<BlockUtil.FoundRectangle>> original,
            @Share(
                    namespace = WorldPortal.MOD_ID,
                    value = "hasHA"
            ) LocalBooleanRef hasHARef
    ) {
        Level level = ((IBlockPos) origin).worldportal$getLevel();
        boolean hasHA = level == null || level
                .getBlockState(((IBlockPos) origin).worldportal$getPortalEntrancePos())
                .hasProperty(BlockStateProperties.HORIZONTAL_AXIS);
        hasHARef.set(hasHA);

        Optional<BlockUtil.FoundRectangle> foundRectangle = original.call(origin, portalAxis);
        if (foundRectangle.isEmpty()) {
            return foundRectangle;
        }

        PortalConfig portalConfig = ((IBlockPos) origin).worldportal$getPortalConfig();
        if (portalConfig != null) {
            BlockUtil.FoundRectangle foundRectangleX = foundRectangle.get();

            Direction.Axis AxisX = Direction.Axis.X;
            Direction.Axis AxisZ = Direction.Axis.Z;

            Identifier frameC1 = portalConfig.getFrameBottomLeftLocation();
            if (frameC1 != null) {
                Block block = BuiltInRegistries.BLOCK.getValue(frameC1);
                this.level.setBlockAndUpdate(foundRectangleX.minCorner.offset(
                        portalAxis == AxisX ? foundRectangleX.axis1Size : 0,
                        hasHA ? -1 : 0,
                        hasHA ? portalAxis == AxisZ ? foundRectangleX.axis1Size : 0 : -1
                ), block.defaultBlockState());
            }
            Identifier frameC2 = portalConfig.getFrameBottomRightLocation();
            if (frameC2 != null) {
                Block block = BuiltInRegistries.BLOCK.getValue(frameC2);
                this.level.setBlockAndUpdate(foundRectangleX.minCorner.offset(
                        portalAxis == AxisX ? -1 : 0,
                        hasHA ? -1 : 0,
                        hasHA && portalAxis != AxisZ ? 0 : -1
                ), block.defaultBlockState());
            }
            Identifier frameC3 = portalConfig.getFrameTopLeftLocation();
            if (frameC3 != null) {
                Block block = BuiltInRegistries.BLOCK.getValue(frameC3);
                this.level.setBlockAndUpdate(foundRectangleX.minCorner.offset(
                        portalAxis == AxisX ? foundRectangleX.axis1Size : 0,
                        hasHA ? foundRectangleX.axis2Size : 0,
                        hasHA ? portalAxis == AxisZ
                                ? foundRectangleX.axis1Size
                                : 0 : foundRectangleX.axis2Size
                ), block.defaultBlockState());
            }
            Identifier frameC4 = portalConfig.getFrameTopRightLocation();
            if (frameC4 != null) {
                Block block = BuiltInRegistries.BLOCK.getValue(frameC4);
                this.level.setBlockAndUpdate(foundRectangleX.minCorner.offset(
                        portalAxis == AxisX ? -1 : 0,
                        hasHA ? foundRectangleX.axis2Size : 0,
                        hasHA ? portalAxis == AxisZ ? -1 : 0 : foundRectangleX.axis2Size
                ), block.defaultBlockState());
            }
        }

        return foundRectangle;
    }

    @WrapOperation(
            method = "createPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;setWithOffset(Lnet/minecraft/core/Vec3i;III)Lnet/minecraft/core/BlockPos$MutableBlockPos;",
                    ordinal = 0
            )
    )
    private BlockPos.MutableBlockPos createPortalSupport(
            BlockPos.MutableBlockPos instance,
            Vec3i pos,
            int x,
            int y,
            int z,
            Operation<BlockPos.MutableBlockPos> original,
            @Share(
                    namespace = WorldPortal.MOD_ID,
                    value = "hasHA"
            ) LocalBooleanRef hasHARef
    ) {
        return original.call(
                instance,
                pos,
                x,
                y,
                hasHARef.get() ? z : z + 1
        );
    }

    @WrapOperation(
            method = "createPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;setWithOffset(Lnet/minecraft/core/Vec3i;III)Lnet/minecraft/core/BlockPos$MutableBlockPos;",
                    ordinal = 1
            )
    )
    private BlockPos.MutableBlockPos createPortalFrame(
            BlockPos.MutableBlockPos instance,
            Vec3i pos,
            int x,
            int y,
            int z,
            Operation<BlockPos.MutableBlockPos> original,
            @Share(
                    namespace = WorldPortal.MOD_ID,
                    value = "hasHA"
            ) LocalBooleanRef hasHARef
    ) {
        boolean hasHA = hasHARef.get();

        return original.call(
                instance,
                pos,
                x,
                hasHA ? y : 0,
                hasHA ? z : y
        );
    }

    @ModifyExpressionValue(
            method = "createPortal",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/block/Blocks;NETHER_PORTAL:Lnet/minecraft/world/level/block/Block;"
            )
    )
    private Block createPortalBlock(
            Block original,
            @Share(
                    namespace = WorldPortal.MOD_ID,
                    value = "hasHA"
            ) LocalBooleanRef hasHARef
    ) {
        return hasHARef.get() ? original : Blocks.END_PORTAL;
    }

    @WrapOperation(
            method = "createPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;"
            )
    )
    private Object createPortalSetPropertySkip(
            BlockState instance,
            Property<?> property,
            Comparable<?> comparable,
            Operation<Object> original,
            @Share(
                    namespace = WorldPortal.MOD_ID,
                    value = "hasHA"
            ) LocalBooleanRef hasHARef
    ) {
        return hasHARef.get() ? original.call(instance, property, comparable) : instance;
    }

    @WrapOperation(
            method = "createPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;setWithOffset(Lnet/minecraft/core/Vec3i;III)Lnet/minecraft/core/BlockPos$MutableBlockPos;",
                    ordinal = 2
            )
    )
    private BlockPos.MutableBlockPos createPortalBlock(
            BlockPos.MutableBlockPos instance,
            Vec3i pos,
            int x,
            int y,
            int z,
            Operation<BlockPos.MutableBlockPos> original,
            @Share(
                    namespace = WorldPortal.MOD_ID,
                    value = "hasHA"
            ) LocalBooleanRef hasHARef
    ) {
        boolean hasHA = hasHARef.get();

        return original.call(
                instance,
                pos,
                x,
                hasHA ? y : 0,
                hasHA ? z : y
        );
    }
}
