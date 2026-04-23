package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalConfig;
import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntity {
    @Unique
    private Identifier portalId;

    @Shadow public abstract Level level();

    @WrapOperation(
            method = "handlePortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;"
            )
    )
    private Entity handlePortalChangeDimensionFinish(
            Entity instance,
            TeleportTransition transition,
            Operation<Entity> original
    ) {
        PortalConfig portalConfig = this.worldportal$getPortalConfig();
        if (portalConfig != null) {
            ServerLevel serverLevel = transition.newLevel();

            ResourceKey<Level> dimension = serverLevel.dimension();
            if (dimension != portalConfig.getDestinationKey()) {
                return original.call(instance, transition);
            }

            BlockPos pos = BlockPos.containing(transition.position());

            BlockState state = serverLevel.getBlockState(pos);
            boolean hasHA = state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS);
            Direction.Axis axis = hasHA
                    ? state.getValue(BlockStateProperties.HORIZONTAL_AXIS)
                    : Direction.Axis.X;
            BlockUtil.FoundRectangle largestRectangleAround = BlockUtil.getLargestRectangleAround(
                    pos,
                    axis,
                    21,
                    hasHA ? Direction.Axis.Y : Direction.Axis.X,
                    21,
                    posX -> serverLevel.getBlockState(posX) == state
            );

            IServerLevel serverLevelX = (IServerLevel) serverLevel;

            PortalData portalData = serverLevelX.worldportal$getPortalData();
            ResourceKey<Block> resourceKey = ResourceKey.create(
                    Registries.BLOCK,
                    BuiltInRegistries.BLOCK.getKey(state.getBlock())
            );
            for (int i = 0; i < largestRectangleAround.axis1Size; i++) {
                for (int j = 0; j < largestRectangleAround.axis2Size; j++) {
                    portalData.putBlock(
                            largestRectangleAround.minCorner.offset(
                                    axis == Direction.Axis.X ? i : 0,
                                    hasHA ? j : 0,
                                    hasHA ? axis == Direction.Axis.Z ? i : 0 : j
                            ),
                            resourceKey
                    );
                }
            }

            portalData.putDimension(largestRectangleAround.minCorner, this.level().dimension());
        }

        return original.call(instance, transition);
    }

    @Inject(method = "handlePortal", at = @At(value = "RETURN"))
    private void handlePortalChangeDimensionAfter(CallbackInfo ci) {
        this.portalId = null;
    }

    @Override
    public Identifier worldportal$getPortalId() {
        return this.portalId;
    }

    @Override
    public PortalConfig worldportal$getPortalConfig() {
        return PortalManager.getPortalConfig(this.worldportal$getPortalId());
    }

    @Override
    public void worldportal$setPortal(Identifier portalId) {
        this.portalId = portalId;
    }

    @Override
    public ResourceKey<Level> worldportal$setupPortal(
            BlockPos pos,
            ResourceKey<Level> originalKey
    ) {
        Level level = this.level();

        BlockState state = level.getBlockState(pos);
        boolean hasHA = state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS);
        Direction.Axis axis = hasHA
                ? state.getValue(BlockStateProperties.HORIZONTAL_AXIS)
                : Direction.Axis.X;
        BlockUtil.FoundRectangle largestRectangleAround = BlockUtil.getLargestRectangleAround(
                pos,
                axis,
                21,
                hasHA ? Direction.Axis.Y : Direction.Axis.Z,
                21,
                posX -> level.getBlockState(posX) == state
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

        Map<Identifier, PortalConfig> portalConfigs = new HashMap<>();

        PortalManager.getPortalConfigs().forEach((k, v) -> {
            Identifier mode = v.getModeLocation();
            if (hasHA) {
                if (mode != null && !mode.equals(PortalConfig.DEFAULT_MODE)) {
                    return;
                }
            } else {
                if (mode == null || !mode.equals(PortalConfig.HORIZONTAL_MODE)) {
                    return;
                }
            }

            Identifier c1 = v.getFrameBottomLeftLocation();
            if (c1 != null && !frameC1.equals(c1)) {
                return;
            }

            Identifier c2 = v.getFrameBottomRightLocation();
            if (c2 != null && !frameC2.equals(c2)) {
                return;
            }

            Identifier c3 = v.getFrameTopLeftLocation();
            if (c3 != null && !frameC3.equals(c3)) {
                return;
            }

            Identifier c4 = v.getFrameTopRightLocation();
            if (c4 != null && !frameC4.equals(c4)) {
                return;
            }

            portalConfigs.put(k, v);
        });

        IServerLevel serverLevelX = (IServerLevel) level;
        ResourceKey<Level> resourceKeyX = level.dimension();

        if (!portalConfigs.isEmpty()) {
            PortalData portalData = serverLevelX.worldportal$getPortalData();

            ResourceKey<Block> resourceKey = ResourceKey.create(
                    Registries.BLOCK,
                    BuiltInRegistries.BLOCK.getKey(state.getBlock())
            );
            for (int i = 0; i < largestRectangleAround.axis1Size; i++) {
                for (int j = 0; j < largestRectangleAround.axis2Size; j++) {
                    portalData.putBlock(
                            largestRectangleAround.minCorner.offset(
                                    axis == Direction.Axis.X ? i : 0,
                                    hasHA ? j : 0,
                                    hasHA ? axis == Direction.Axis.Z ? i : 0 : j
                            ),
                            resourceKey
                    );
                }
            }

            ResourceKey<Level> resourceKeyZ = portalData.getDimension(minCornerPos);
            if (resourceKeyZ != null) {
                for (Map.Entry<Identifier, PortalConfig> entry : portalConfigs.entrySet()) {
                    ResourceKey<Level> resourceKeyV = entry.getValue().getDestinationKey();
                    if (resourceKeyX != resourceKeyV) {
                        continue;
                    }

                    this.worldportal$setPortal(entry.getKey());

                    return resourceKeyZ;
                }
            }

            Map<BlockPos, ResourceKey<Level>> dimensions = portalData.getDimensions();
            for (Map.Entry<BlockPos, ResourceKey<Level>> entry : dimensions.entrySet()) {
                BlockPos minCornerPosX = entry.getKey();
                if (pos.distSqr(minCornerPosX) > 128) {
                    continue;
                }

                ResourceKey<Level> resourceKeyB = entry.getValue();

                for (Map.Entry<Identifier, PortalConfig> entryX : portalConfigs.entrySet()) {
                    ResourceKey<Level> resourceKeyA = entryX.getValue().getDestinationKey();
                    if (resourceKeyX != resourceKeyA) {
                        continue;
                    }

                    portalData.putDimension(minCornerPos, resourceKeyB);
                    portalData.removeDimension(minCornerPosX);

                    this.worldportal$setPortal(entryX.getKey());

                    return resourceKeyB;
                }
            }
        }

        portalConfigs.keySet().removeIf(k -> {
            return resourceKeyX == portalConfigs.get(k).getDestinationKey();
        });

        if (!portalConfigs.isEmpty()) {
            int random = level.getRandom().nextInt(portalConfigs.size());

            int i = 0;
            for (Map.Entry<Identifier, PortalConfig> entry : portalConfigs.entrySet()) {
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

        return hasHA ? originalKey : null;
    }
}
