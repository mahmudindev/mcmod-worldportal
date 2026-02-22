package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalPositions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Function;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {
    @Inject(
            method = "onPlace",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;canSurvive(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    private void onPlaceHorizontalPortal(
            BlockState blockState,
            Level level,
            BlockPos blockPos,
            BlockState blockState2,
            boolean bl,
            CallbackInfo ci
    ) {
        boolean inPortalDimension = false;

        Map<ResourceLocation, PortalData> portals = PortalManager.getPortals();
        for (Map.Entry<ResourceLocation, PortalData> entry : portals.entrySet()) {
            if (inPortalDimension) {
                break;
            }

            PortalData portal = entry.getValue();

            ResourceLocation mode = portal.getModeLocation();
            if (mode == null || !mode.equals(PortalData.HORIZONTAL_MODE)) {
                continue;
            }

            if (portal.getDestinationKey() != level.dimension()) {
                inPortalDimension = true;
            }
        }

        Function<BlockState, Boolean> isEmpty = (blockStateX) -> {
            if (blockStateX.isAir() || blockStateX.is(BlockTags.FIRE)) {
                return true;
            }

            return blockStateX.is(Blocks.END_PORTAL);
        };

        if (!inPortalDimension || !isEmpty.apply(blockState)) {
            return;
        }

        BlockPos blockPosX = blockPos;
        while (true) {
            if (Math.abs(blockPos.getX() - blockPosX.getX()) > 21) {
                return;
            }

            if (Math.abs(blockPos.getZ() - blockPosX.getZ()) > 21) {
                return;
            }

            BlockPos blockPosH = blockPosX.north();

            BlockState blockStateN = level.getBlockState(blockPosH);
            if (!isEmpty.apply(blockStateN)) {
                if (!blockStateN.is(Blocks.OBSIDIAN)) {
                    return;
                }

                blockPosH = blockPosX.west();

                BlockState blockStateW = level.getBlockState(blockPosH);
                if (!isEmpty.apply(blockStateW)) {
                    if (!blockStateW.is(Blocks.OBSIDIAN)) {
                        return;
                    }

                    break;
                }
            }

            blockPosX = blockPosH;
        }

        int distanceS = 1;
        for (int i = 1; i <= 21; i++) {
            BlockPos blockPosS = blockPosX.south(i);

            BlockState blockStateS = level.getBlockState(blockPosS);
            if (!isEmpty.apply(blockStateS)) {
                if (!blockStateS.is(Blocks.OBSIDIAN)) {
                    return;
                }

                break;
            }

            if (!level.getBlockState(blockPosS.west()).is(Blocks.OBSIDIAN)) {
                return;
            }

            distanceS++;
        }

        int distanceE = 1;
        for (int i = 1; i <= 21; i++) {
            BlockPos blockPosE = blockPosX.east(i);

            BlockState blockStateE = level.getBlockState(blockPosE);
            if (!isEmpty.apply(blockStateE)) {
                if (!blockStateE.is(Blocks.OBSIDIAN)) {
                    return;
                }

                break;
            }

            if (!level.getBlockState(blockPosE.north()).is(Blocks.OBSIDIAN)) {
                return;
            }

            distanceE++;
        }

        if ((distanceS < 3 && distanceE < 2) || (distanceS < 2 && distanceE < 3)) {
            return;
        }

        for (int x = 1; x < distanceE; x++) {
            for (int z = 1; z < distanceS; z++) {
                BlockPos blockPosH = blockPosX.offset(x, 0, z);

                if (x == distanceE - 1) {
                    if (!level.getBlockState(blockPosH.east()).is(Blocks.OBSIDIAN)) {
                        return;
                    }
                }

                if (z == distanceS - 1) {
                    if (!level.getBlockState(blockPosH.south()).is(Blocks.OBSIDIAN)) {
                        return;
                    }
                }

                if (!isEmpty.apply(level.getBlockState(blockPosH))) {
                    return;
                }
            }
        }

        IServerLevel serverLevelX = (IServerLevel) level;
        PortalPositions portalPositions = serverLevelX.worldportal$getPortalPositions();
        Block block = Blocks.END_PORTAL;
        ResourceKey<Block> resourceKey = ResourceKey.create(
                Registries.BLOCK,
                BuiltInRegistries.BLOCK.getKey(block)
        );

        for (int x = 0; x < distanceE; x++) {
            for (int z = 0; z < distanceS; z++) {
                BlockPos blockPosZ = blockPosX.offset(x, 0, z);

                level.setBlock(blockPosZ, block.defaultBlockState(), 18);

                portalPositions.putBlock(blockPosZ, resourceKey);
            }
        }
    }
}
