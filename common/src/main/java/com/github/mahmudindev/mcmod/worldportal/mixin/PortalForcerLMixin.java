package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.github.mahmudindev.mcmod.worldportal.base.IBlockPos;
import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalPositions;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.BlockUtil;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
            BlockPos blockPos,
            int i,
            PoiManager.Occupancy occupancy,
            Operation<Stream<PoiRecord>> original
    ) {
        Stream<PoiRecord> poiRecordStream = original.call(
                instance,
                predicate,
                blockPos,
                i,
                occupancy
        );

        IServerLevel serverLevelX = (IServerLevel) this.level;
        PortalPositions portalPositions = serverLevelX.worldportal$getPortalPositions();
        ResourceKey<Block> blockResourceKey = ResourceKey.create(
                Registries.BLOCK,
                BuiltInRegistries.BLOCK.getKey(Blocks.NETHER_PORTAL)
        );
        ResourceKey<PoiType> poiTypeResourceKey = PoiTypes.NETHER_PORTAL;
        PoiType poiType = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getValue(poiTypeResourceKey);
        Holder<PoiType> poiTypeHolder = Holder.direct(poiType);
        Stream<PoiRecord> poiRecordStreamX = ChunkPos.rangeClosed(
                new ChunkPos(blockPos),
                Math.floorDiv(i, 16) + 1
        ).flatMap(chunkPos -> IntStream.range(
                this.level.getMinSectionY(),
                this.level.getMaxSectionY()
        ).boxed().flatMap(height -> SectionPos.of(
                chunkPos,
                height
        ).blocksInside()).map(blockPosX -> {
            ResourceKey<Block> blockResourceKeyX = portalPositions.getBlock(blockPosX);

            if (blockResourceKeyX == null || blockResourceKeyX == blockResourceKey) {
                return null;
            }

            ChunkAccess chunkAccess = this.level.getChunk(blockPosX);
            BlockState blockState = chunkAccess.getBlockState(blockPosX);
            if (ResourceKey.create(
                    Registries.BLOCK,
                    BuiltInRegistries.BLOCK.getKey(blockState.getBlock())
            ) != blockResourceKeyX) {
                portalPositions.removeBlock(blockPosX);

                return null;
            }

            return new PoiRecord(blockPosX, poiTypeHolder, () -> {});
        }).filter(Objects::nonNull)).filter(poiRecord -> {
            BlockPos blockPosX = poiRecord.getPos();
            if (Math.abs(blockPosX.getX() - blockPos.getX()) > i) {
                return false;
            }

            if (Math.abs(blockPosX.getZ() - blockPos.getZ()) > i) {
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
            BlockPos blockPos
    ) {
        Map<BlockPos, Boolean> blockPosPassMap = new HashMap<>();
        PortalData portal = ((IBlockPos) blockPos).worldportal$getPortal();
        Level level = ((IBlockPos) blockPos).worldportal$getLevel();
        boolean hasHA = level == null || level
                .getBlockState(((IBlockPos) blockPos).worldportal$getPortalEntrancePos())
                .hasProperty(BlockStateProperties.HORIZONTAL_AXIS);

        return original.call(instance, predicate).filter(blockPosX -> {

            Boolean blockPosPass = blockPosPassMap.get(blockPosX);
            if (blockPosPass != null) {
                return blockPosPass;
            }

            Direction.Axis AxisX = Direction.Axis.X;
            Direction.Axis AxisY = Direction.Axis.Y;
            Direction.Axis AxisZ = Direction.Axis.Z;

            BlockState blockState = this.level.getBlockState(blockPosX);
            boolean hasHAX = blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS);
            Direction.Axis axis = hasHAX
                    ? blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS)
                    : AxisX;
            BlockUtil.FoundRectangle foundRectangle = BlockUtil.getLargestRectangleAround(
                    blockPosX,
                    axis,
                    21,
                    hasHAX ? AxisY : AxisZ,
                    21,
                    blockPosZ -> this.level.getBlockState(blockPosZ) == blockState
            );

            ResourceLocation frameC1 = BuiltInRegistries.BLOCK.getKey(
                    this.level.getBlockState(foundRectangle.minCorner.offset(
                            axis == AxisX ? foundRectangle.axis1Size : 0,
                            hasHAX ? -1 : 0,
                            hasHAX ? axis == AxisZ ? foundRectangle.axis1Size : 0 : -1
                    )).getBlock()
            );
            ResourceLocation frameC2 = BuiltInRegistries.BLOCK.getKey(
                    this.level.getBlockState(foundRectangle.minCorner.offset(
                            axis == AxisX ? -1 : 0,
                            hasHAX ? -1 : 0,
                            hasHAX && axis != AxisZ ? 0 : -1
                    )).getBlock()
            );
            ResourceLocation frameC3 = BuiltInRegistries.BLOCK.getKey(
                    this.level.getBlockState(foundRectangle.minCorner.offset(
                            axis == AxisX ? foundRectangle.axis1Size : 0,
                            hasHAX ? foundRectangle.axis2Size : 0,
                            hasHAX ? axis == AxisZ
                                    ? foundRectangle.axis1Size
                                    : 0 : foundRectangle.axis2Size
                    )).getBlock()
            );
            ResourceLocation frameC4 = BuiltInRegistries.BLOCK.getKey(
                    this.level.getBlockState(foundRectangle.minCorner.offset(
                            axis == AxisX ? -1 : 0,
                            hasHAX ? foundRectangle.axis2Size : 0,
                            hasHAX ? axis == AxisZ ? -1 : 0 : foundRectangle.axis2Size
                    )).getBlock()
            );

            boolean blockPosPassX = false;

            if (portal != null) {
                blockPosPassX = hasHA == hasHAX;

                if (blockPosPassX) {
                    for (ResourceLocation[] v : new ResourceLocation[][]{
                            {frameC1, portal.getFrameBottomLeftLocation()},
                            {frameC2, portal.getFrameBottomRightLocation()},
                            {frameC3, portal.getFrameTopLeftLocation()},
                            {frameC4, portal.getFrameTopRightLocation()}
                    }) {
                        if (v[1] != null && v[0].equals(v[1])) {
                            continue;
                        }

                        blockPosPassX = false;
                        break;
                    }
                }
            } else if (hasHAX) {
                blockPosPassX = true;

                Map<ResourceLocation, PortalData> portals = PortalManager.getPortals();
                for (Map.Entry<ResourceLocation, PortalData> entry : portals.entrySet()) {
                    PortalData portalX = entry.getValue();

                    ResourceLocation mode = portalX.getModeLocation();
                    if (mode != null && !mode.equals(PortalData.DEFAULT_MODE)) {
                        continue;
                    }

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
                            axis == AxisX ? i : 0,
                            hasHAX ? j : 0,
                            hasHAX ? axis == AxisZ ? i : 0 : j
                    ), blockPosPassX);
                }
            }

            return blockPosPassX;
        });
    }

    @WrapOperation(
            method = "method_61028",
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
            BlockPos blockPos,
            Direction.Axis axis,
            Operation<Optional<BlockUtil.FoundRectangle>> original,
            @Share(
                    namespace = WorldPortal.MOD_ID,
                    value = "hasHA"
            ) LocalBooleanRef hasHARef
    ) {
        Level level = ((IBlockPos) blockPos).worldportal$getLevel();
        boolean hasHA = level == null || level
                .getBlockState(((IBlockPos) blockPos).worldportal$getPortalEntrancePos())
                .hasProperty(BlockStateProperties.HORIZONTAL_AXIS);
        hasHARef.set(hasHA);

        Optional<BlockUtil.FoundRectangle> oFoundRectangle = original.call(blockPos, axis);
        if (oFoundRectangle.isEmpty()) {
            return oFoundRectangle;
        }

        PortalData portal = ((IBlockPos) blockPos).worldportal$getPortal();
        if (portal != null) {
            BlockUtil.FoundRectangle foundRectangle = oFoundRectangle.get();

            Direction.Axis AxisX = Direction.Axis.X;
            Direction.Axis AxisZ = Direction.Axis.Z;

            ResourceLocation frameC1 = portal.getFrameBottomLeftLocation();
            if (frameC1 != null) {
                Block block = BuiltInRegistries.BLOCK.getValue(frameC1);
                this.level.setBlockAndUpdate(foundRectangle.minCorner.offset(
                        axis == AxisX ? foundRectangle.axis1Size : 0,
                        hasHA ? -1 : 0,
                        hasHA ? axis == AxisZ ? foundRectangle.axis1Size : 0 : -1
                ), block.defaultBlockState());
            }
            ResourceLocation frameC2 = portal.getFrameBottomRightLocation();
            if (frameC2 != null) {
                Block block = BuiltInRegistries.BLOCK.getValue(frameC2);
                this.level.setBlockAndUpdate(foundRectangle.minCorner.offset(
                        axis == AxisX ? -1 : 0,
                        hasHA ? -1 : 0,
                        hasHA && axis != AxisZ ? 0 : -1
                ), block.defaultBlockState());
            }
            ResourceLocation frameC3 = portal.getFrameTopLeftLocation();
            if (frameC3 != null) {
                Block block = BuiltInRegistries.BLOCK.getValue(frameC3);
                this.level.setBlockAndUpdate(foundRectangle.minCorner.offset(
                        axis == AxisX ? foundRectangle.axis1Size : 0,
                        hasHA ? foundRectangle.axis2Size : 0,
                        hasHA ? axis == AxisZ
                                ? foundRectangle.axis1Size
                                : 0 : foundRectangle.axis2Size
                ), block.defaultBlockState());
            }
            ResourceLocation frameC4 = portal.getFrameTopRightLocation();
            if (frameC4 != null) {
                Block block = BuiltInRegistries.BLOCK.getValue(frameC4);
                this.level.setBlockAndUpdate(foundRectangle.minCorner.offset(
                        axis == AxisX ? -1 : 0,
                        hasHA ? foundRectangle.axis2Size : 0,
                        hasHA ? axis == AxisZ ? -1 : 0 : foundRectangle.axis2Size
                ), block.defaultBlockState());
            }
        }

        return oFoundRectangle;
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
            Vec3i vec3i,
            int i,
            int j,
            int k,
            Operation<BlockPos.MutableBlockPos> original,
            @Share(
                    namespace = WorldPortal.MOD_ID,
                    value = "hasHA"
            ) LocalBooleanRef hasHARef
    ) {
        return original.call(
                instance,
                vec3i,
                i,
                j,
                hasHARef.get() ? k : k + 1
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
            Vec3i vec3i,
            int i,
            int j,
            int k,
            Operation<BlockPos.MutableBlockPos> original,
            @Share(
                    namespace = WorldPortal.MOD_ID,
                    value = "hasHA"
            ) LocalBooleanRef hasHARef
    ) {
        boolean hasHA = hasHARef.get();

        return original.call(
                instance,
                vec3i,
                i,
                hasHA ? j : 0,
                hasHA ? k : j
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
            Vec3i vec3i,
            int i,
            int j,
            int k,
            Operation<BlockPos.MutableBlockPos> original,
            @Share(
                    namespace = WorldPortal.MOD_ID,
                    value = "hasHA"
            ) LocalBooleanRef hasHARef
    ) {
        boolean hasHA = hasHARef.get();

        return original.call(
                instance,
                vec3i,
                i,
                hasHA ? j : 0,
                hasHA ? k : j
        );
    }
}
