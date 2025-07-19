package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.github.mahmudindev.mcmod.worldportal.base.IBlockPos;
import com.github.mahmudindev.mcmod.worldportal.core.WorldPortalPoiTypes;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
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
            method = "method_22389",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z"
            )
    )
    private static boolean method22389IncludePoiType(
            Holder<PoiType> instance,
            ResourceKey<PoiType> resourceKey,
            Operation<Boolean> original
    ) {
        if (instance.is(WorldPortalPoiTypes.END_PORTAL)) {
            return true;
        }

        return original.call(instance, resourceKey);
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

        Optional<BlockUtil.FoundRectangle> optional = original.call(blockPos, axis);
        if (optional.isEmpty()) {
            return optional;
        }

        PortalData portal = ((IBlockPos) blockPos).worldportal$getPortal();
        if (portal != null) {
            BlockUtil.FoundRectangle foundRectangle = optional.get();

            Direction.Axis AxisX = Direction.Axis.X;
            Direction.Axis AxisZ = Direction.Axis.Z;

            ResourceLocation frameC1 = portal.getFrameBottomLeftLocation();
            if (frameC1 != null) {
                Block block = BuiltInRegistries.BLOCK.get(frameC1);
                this.level.setBlockAndUpdate(foundRectangle.minCorner.offset(
                        axis == AxisX ? foundRectangle.axis1Size : 0,
                        hasHA ? -1 : 0,
                        hasHA ? axis == AxisZ ? foundRectangle.axis1Size : 0 : -1
                ), block.defaultBlockState());
            }
            ResourceLocation frameC2 = portal.getFrameBottomRightLocation();
            if (frameC2 != null) {
                Block block = BuiltInRegistries.BLOCK.get(frameC2);
                this.level.setBlockAndUpdate(foundRectangle.minCorner.offset(
                        axis == AxisX ? -1 : 0,
                        hasHA ? -1 : 0,
                        hasHA && axis != AxisZ ? 0 : -1
                ), block.defaultBlockState());
            }
            ResourceLocation frameC3 = portal.getFrameTopLeftLocation();
            if (frameC3 != null) {
                Block block = BuiltInRegistries.BLOCK.get(frameC3);
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
                Block block = BuiltInRegistries.BLOCK.get(frameC4);
                this.level.setBlockAndUpdate(foundRectangle.minCorner.offset(
                        axis == AxisX ? -1 : 0,
                        hasHA ? foundRectangle.axis2Size : 0,
                        hasHA ? axis == AxisZ ? -1 : 0 : foundRectangle.axis2Size
                ), block.defaultBlockState());
            }
        }

        return optional;
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
            BlockPos blockPos,
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
            BlockPos blockPos,
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
            BlockPos blockPos,
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
