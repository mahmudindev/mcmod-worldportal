package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IBlockPos;
import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalReturns;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntity {
    @Shadow protected BlockPos portalEntrancePos;
    @Unique
    private ResourceLocation portalId;

    @Shadow public abstract Level level();

    @Inject(
            method = "handleNetherPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;",
                    shift = At.Shift.AFTER
            )
    )
    private void handleNetherPortalChangeDimensionAfter(CallbackInfo ci) {
        this.portalId = null;
    }

    @WrapOperation(
            method = "getExitPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/portal/PortalForcer;findPortalAround(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Ljava/util/Optional;"
            )
    )
    private Optional<BlockUtil.FoundRectangle> getExitPortalForcerFindPrepare(
            PortalForcer instance,
            BlockPos blockPos,
            boolean isNether,
            WorldBorder worldBorder,
            Operation<Optional<BlockUtil.FoundRectangle>> original
    ) {
        PortalData portal = this.worldportal$getPortal();
        if (portal != null) {
            ((IBlockPos) blockPos).worldportal$setLevel(this.level());
            ((IBlockPos) blockPos).worldportal$setPortalEntrancePos(this.portalEntrancePos);
            ((IBlockPos) blockPos).worldportal$setPortal(this.worldportal$getPortalId());
        }

        return original.call(instance, blockPos, isNether, worldBorder);
    }

    @Override
    public ResourceLocation worldportal$getPortalId() {
        return this.portalId;
    }

    @Override
    public PortalData worldportal$getPortal() {
        return PortalManager.getPortal(this.worldportal$getPortalId());
    }

    @Override
    public void worldportal$setPortal(ResourceLocation portalId) {
        this.portalId = portalId;
    }

    @Override
    public ResourceKey<Level> worldportal$setupPortal(ResourceKey<Level> originalKey) {
        Level level = this.level();

        BlockState blockState = level.getBlockState(this.portalEntrancePos);
        boolean hasHA = blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS);
        Direction.Axis axis = hasHA
                ? blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS)
                : Direction.Axis.X;
        BlockUtil.FoundRectangle foundRectangle = BlockUtil.getLargestRectangleAround(
                this.portalEntrancePos,
                axis,
                21,
                hasHA ? Direction.Axis.Y : Direction.Axis.Z,
                21,
                blockPosX -> level.getBlockState(blockPosX) == blockState
        );

        BlockPos minCornerPos = foundRectangle.minCorner;

        ResourceLocation frameC1 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? foundRectangle.axis1Size : 0,
                        hasHA ? -1 : 0,
                        hasHA ? axis == Direction.Axis.Z ? foundRectangle.axis1Size : 0 : -1
                )).getBlock()
        );
        ResourceLocation frameC2 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        hasHA ? -1 : 0,
                        hasHA && axis != Direction.Axis.Z ? 0 : -1
                )).getBlock()
        );
        ResourceLocation frameC3 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? foundRectangle.axis1Size : 0,
                        hasHA ? foundRectangle.axis2Size : 0,
                        hasHA ? axis == Direction.Axis.Z
                                ? foundRectangle.axis1Size
                                : 0 : foundRectangle.axis2Size
                )).getBlock()
        );
        ResourceLocation frameC4 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        hasHA ? foundRectangle.axis2Size : 0,
                        hasHA ? axis == Direction.Axis.Z ? -1 : 0 : foundRectangle.axis2Size
                )).getBlock()
        );

        Map<ResourceLocation, PortalData> portals = new HashMap<>();

        PortalManager.getPortals().forEach((k, v) -> {
            ResourceLocation mode = v.getModeLocation();
            if (hasHA) {
                if (mode != null && !mode.equals(PortalData.DEFAULT_MODE)) {
                    return;
                }
            } else {
                if (mode == null || !mode.equals(PortalData.HORIZONTAL_MODE)) {
                    return;
                }
            }

            ResourceLocation c1 = v.getFrameBottomLeftLocation();
            if (c1 != null && !frameC1.equals(c1)) {
                return;
            }

            ResourceLocation c2 = v.getFrameBottomRightLocation();
            if (c2 != null && !frameC2.equals(c2)) {
                return;
            }

            ResourceLocation c3 = v.getFrameTopLeftLocation();
            if (c3 != null && !frameC3.equals(c3)) {
                return;
            }

            ResourceLocation c4 = v.getFrameTopRightLocation();
            if (c4 != null && !frameC4.equals(c4)) {
                return;
            }

            portals.put(k, v);
        });

        ResourceKey<Level> resourceKeyX = level.dimension();

        IServerLevel serverLevelX = (IServerLevel) level;
        PortalReturns portalReturns = serverLevelX.worldportal$getPortalReturns();

        if (!portals.isEmpty()) {
            ResourceKey<Level> resourceKeyZ = portalReturns.getDimension(minCornerPos);
            if (resourceKeyZ != null) {
                for (Map.Entry<ResourceLocation, PortalData> entry : portals.entrySet()) {
                    ResourceKey<Level> resourceKeyV = entry.getValue().getDestinationKey();
                    if (resourceKeyX != resourceKeyV) {
                        continue;
                    }

                    this.worldportal$setPortal(entry.getKey());

                    return resourceKeyZ;
                }
            }

            Map<BlockPos, ResourceKey<Level>> dimensions = portalReturns.getDimensions();
            for (Map.Entry<BlockPos, ResourceKey<Level>> entry : dimensions.entrySet()) {
                BlockPos minCornerPosX = entry.getKey();
                if (this.portalEntrancePos.distSqr(minCornerPosX) > 128) {
                    continue;
                }

                ResourceKey<Level> resourceKeyB = entry.getValue();

                for (Map.Entry<ResourceLocation, PortalData> entryX : portals.entrySet()) {
                    ResourceKey<Level> resourceKeyA = entryX.getValue().getDestinationKey();
                    if (resourceKeyX != resourceKeyA) {
                        continue;
                    }

                    portalReturns.putDimension(minCornerPos, resourceKeyB);
                    portalReturns.removeDimension(minCornerPosX);

                    this.worldportal$setPortal(entryX.getKey());

                    return resourceKeyB;
                }
            }
        }

        portals.keySet().removeIf(k -> resourceKeyX == portals.get(k).getDestinationKey());

        if (!portals.isEmpty()) {
            int random = level.getRandom().nextInt(portals.size());

            int i = 0;
            for (Map.Entry<ResourceLocation, PortalData> entry : portals.entrySet()) {
                if (i != random) {
                    i++;

                    continue;
                }

                ResourceKey<Level> resourceKeyZ = entry.getValue().getDestinationKey();
                if (resourceKeyZ != null) {
                    this.worldportal$setPortal(entry.getKey());

                    return resourceKeyZ;
                }
            }
        }

        portalReturns.removeDimension(minCornerPos);

        return hasHA ? originalKey : null;
    }
}
