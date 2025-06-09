package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalReturns;
import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = Entity.class, priority = 750)
public abstract class EntityLMixin implements IEntity {
    @Shadow protected BlockPos portalEntrancePos;

    @Shadow public abstract Level level();

    @WrapOperation(
            method = "handleNetherPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"
            )
    )
    private ServerLevel handleNetherPortalPrepare(
            MinecraftServer instance,
            ResourceKey<Level> resourceKey,
            Operation<ServerLevel> original
    ) {
        Level level = this.level();

        BlockState blockState = level.getBlockState(this.portalEntrancePos);
        if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            return original.call(instance, resourceKey);
        }

        Direction.Axis axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
        BlockUtil.FoundRectangle foundRectangle = BlockUtil.getLargestRectangleAround(
                this.portalEntrancePos,
                axis,
                21,
                Direction.Axis.Y,
                21,
                blockPosX -> level.getBlockState(blockPosX) == blockState
        );

        BlockPos minCornerPos = foundRectangle.minCorner;

        ResourceLocation frameC1 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        -1,
                        axis == Direction.Axis.Z ? -1 : 0
                )).getBlock()
        );
        ResourceLocation frameC2 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? foundRectangle.axis1Size : 0,
                        -1,
                        axis == Direction.Axis.Z ? foundRectangle.axis1Size : 0
                )).getBlock()
        );
        ResourceLocation frameC3 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        foundRectangle.axis2Size,
                        axis == Direction.Axis.Z ? -1 : 0
                )).getBlock()
        );
        ResourceLocation frameC4 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(minCornerPos.offset(
                        axis == Direction.Axis.X ? foundRectangle.axis1Size : 0,
                        foundRectangle.axis2Size,
                        axis == Direction.Axis.Z ? foundRectangle.axis1Size : 0
                )).getBlock()
        );

        Map<ResourceLocation, PortalData> portals = new HashMap<>();

        PortalManager.getPortals().forEach((k, v) -> {
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

        ResourceKey<Level> dimension = level.dimension();

        IServerLevel serverLevelX = (IServerLevel) level;
        PortalReturns portalReturns = serverLevelX.worldportal$getPortalReturns();

        if (!portals.isEmpty()) {
            ResourceKey<Level> returnDimension = portalReturns.getDimension(minCornerPos);
            if (returnDimension != null) {
                for (Map.Entry<ResourceLocation, PortalData> entry : portals.entrySet()) {
                    if (dimension != entry.getValue().getDestinationKey()) {
                        continue;
                    }

                    this.worldportal$setPortal(entry.getKey());

                    return original.call(instance, returnDimension);
                }
            }
        }

        portals.keySet().removeIf(k -> dimension == portals.get(k).getDestinationKey());

        if (!portals.isEmpty()) {
            int random = level.getRandom().nextInt(portals.size());

            int i = 0;
            for (Map.Entry<ResourceLocation, PortalData> entry : portals.entrySet()) {
                if (i != random) {
                    i++;

                    continue;
                }

                ResourceKey<Level> modified = entry.getValue().getDestinationKey();
                if (modified != null) {
                    this.worldportal$setPortal(entry.getKey());

                    return original.call(instance, modified);
                }
            }
        }

        portalReturns.removeDimension(minCornerPos);

        return original.call(instance, resourceKey);
    }

    @ModifyExpressionValue(
            method = "findDimensionEntryPoint",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;"
            )
    )
    private ResourceKey<Level> findDimensionEntryPointEndKey(ResourceKey<Level> original) {
        if (this.worldportal$getPortal() != null) {
            return null;
        }

        return original;
    }

    @ModifyExpressionValue(
            method = "findDimensionEntryPoint",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;"
            )
    )
    private ResourceKey<Level> findDimensionEntryPointOverworldKey(
            ResourceKey<Level> original
    ) {
        if (this.worldportal$getPortal() != null) {
            return null;
        }

        return original;
    }

    @ModifyExpressionValue(
            method = "findDimensionEntryPoint",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;NETHER:Lnet/minecraft/resources/ResourceKey;",
                    ordinal = 1
            )
    )
    private ResourceKey<Level> findDimensionEntryPointNetherKey1(
            ResourceKey<Level> original
    ) {
        if (this.worldportal$getPortal() != null) {
            return this.level().dimension();
        }

        return original;
    }
}
