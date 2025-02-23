package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalReturns;
import com.github.mahmudindev.mcmod.worldportal.base.IEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntity {
    @Shadow protected BlockPos portalEntrancePos;
    @Unique private PortalData portalEntranceData;

    @Shadow public abstract Level level();

    @ModifyArg(
            method = "handleNetherPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;"
            )
    )
    private ResourceKey<Level> handleNetherPortalPrepare(ResourceKey<Level> original) {
        Level level = this.level();

        BlockState blockState = level.getBlockState(this.portalEntrancePos);
        if (!blockState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            return original;
        }

        Direction.Axis axis = blockState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
        BlockUtil.FoundRectangle portalRectangle = PortalManager.getPortalRectangle(
                level,
                this.portalEntrancePos,
                blockState,
                axis
        );

        BlockPos portalCornerPos = portalRectangle.minCorner;

        ResourceLocation portalL1 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(portalCornerPos.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        -1,
                        axis == Direction.Axis.Z ? -1 : 0
                )).getBlock()
        );
        ResourceLocation portalL2 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(portalCornerPos.offset(
                        axis == Direction.Axis.X ? portalRectangle.axis1Size : 0,
                        -1,
                        axis == Direction.Axis.Z ? portalRectangle.axis1Size : 0
                )).getBlock()
        );
        ResourceLocation portalL3 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(portalCornerPos.offset(
                        axis == Direction.Axis.X ? -1 : 0,
                        portalRectangle.axis2Size,
                        axis == Direction.Axis.Z ? -1 : 0
                )).getBlock()
        );
        ResourceLocation portalL4 = BuiltInRegistries.BLOCK.getKey(
                level.getBlockState(portalCornerPos.offset(
                        axis == Direction.Axis.X ? portalRectangle.axis1Size : 0,
                        portalRectangle.axis2Size,
                        axis == Direction.Axis.Z ? portalRectangle.axis1Size : 0
                )).getBlock()
        );

        List<PortalData> portalDataList = new LinkedList<>();

        PortalManager.getPortals().forEach((k, v) -> {
            ResourceLocation s1 = v.getFrameBottomLeftLocation();
            if (s1 != null && !portalL1.equals(s1)) {
                return;
            }

            ResourceLocation s2 = v.getFrameBottomRightLocation();
            if (s2 != null && !portalL2.equals(s2)) {
                return;
            }

            ResourceLocation s3 = v.getFrameTopLeftLocation();
            if (s3 != null && !portalL3.equals(s3)) {
                return;
            }

            ResourceLocation s4 = v.getFrameTopRightLocation();
            if (s4 != null && !portalL4.equals(s4)) {
                return;
            }

            portalDataList.add(v);
        });

        IServerLevel serverLevelX = (IServerLevel) level;

        ResourceKey<Level> dimension = level.dimension();
        PortalReturns portalReturns = serverLevelX.worldportal$getPortalReturns();

        if (!portalDataList.isEmpty()) {
            ResourceKey<Level> returnDimension = portalReturns.getDimension(portalCornerPos);
            if (returnDimension != null) {
                for (PortalData portalData : portalDataList) {
                    if (dimension != portalData.getDestinationKey()) {
                        continue;
                    }

                    this.portalEntranceData = portalData;

                    return returnDimension;
                }
            }
        }

        portalDataList.removeIf(portalData -> dimension == portalData.getDestinationKey());

        if (!portalDataList.isEmpty()) {
            RandomSource random = level.getRandom();
            PortalData portalData = portalDataList.get(random.nextInt(portalDataList.size()));

            ResourceKey<Level> modified = portalData.getDestinationKey();
            if (modified != null) {
                this.portalEntranceData = portalData;

                return modified;
            }
        }

        portalReturns.removeDimension(portalCornerPos);

        return original;
    }

    @Inject(
            method = "handleNetherPortal",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;",
                    shift = At.Shift.AFTER
            )
    )
    private void handleNetherPortalChangeDimensionAfter(CallbackInfo ci) {
        this.portalEntranceData = null;
    }

    @ModifyExpressionValue(
            method = "findDimensionEntryPoint",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;"
            )
    )
    private ResourceKey<Level> findDimensionEntryPointEndKey(ResourceKey<Level> original) {
        if (((IEntity) this).worldportal$getPortalEntranceData() != null) {
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
        if (((IEntity) this).worldportal$getPortalEntranceData() != null) {
            return null;
        }

        return original;
    }

    @ModifyExpressionValue(
            method = "findDimensionEntryPoint",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/Level;NETHER:Lnet/minecraft/resources/ResourceKey;",
                    ordinal = 0
            )
    )
    private ResourceKey<Level> findDimensionEntryPointNetherKey0(
            ResourceKey<Level> original,
            ServerLevel serverLevel
    ) {
        if (((IEntity) this).worldportal$getPortalEntranceData() != null) {
            return serverLevel.dimension();
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
    private ResourceKey<Level> findDimensionEntryPointNetherKey1(ResourceKey<Level> original) {
        if (((IEntity) this).worldportal$getPortalEntranceData() != null) {
            return this.level().dimension();
        }

        return original;
    }

    @Override
    public PortalData worldportal$getPortalEntranceData() {
        return this.portalEntranceData;
    }
}
