package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalConfig;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalManager;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean movedByPiston,
            CallbackInfo ci
    ) {
        boolean inPortalDimension = false;

        Map<Identifier, PortalConfig> portalConfigs = PortalManager.getPortalConfigs();
        for (Map.Entry<Identifier, PortalConfig> entry : portalConfigs.entrySet()) {
            if (inPortalDimension) {
                break;
            }

            PortalConfig portal = entry.getValue();

            Identifier mode = portal.getModeLocation();
            if (mode == null || !mode.equals(PortalConfig.HORIZONTAL_MODE)) {
                continue;
            }

            if (portal.getDestinationKey() != level.dimension()) {
                inPortalDimension = true;
            }
        }

        Function<BlockState, Boolean> isEmpty = (stateX) -> {
            if (stateX.isAir() || stateX.is(BlockTags.FIRE)) {
                return true;
            }

            return stateX.is(Blocks.END_PORTAL);
        };

        if (!inPortalDimension || !isEmpty.apply(state)) {
            return;
        }

        BlockPos posX = pos;
        while (true) {
            if (Math.abs(pos.getX() - posX.getX()) > 21) {
                return;
            }

            if (Math.abs(pos.getZ() - posX.getZ()) > 21) {
                return;
            }

            BlockPos posH = posX.north();

            BlockState stateN = level.getBlockState(posH);
            if (!isEmpty.apply(stateN)) {
                if (!stateN.is(Blocks.OBSIDIAN)) {
                    return;
                }

                posH = posX.west();

                BlockState stateW = level.getBlockState(posH);
                if (!isEmpty.apply(stateW)) {
                    if (!stateW.is(Blocks.OBSIDIAN)) {
                        return;
                    }

                    break;
                }
            }

            posX = posH;
        }

        int distanceS = 1;
        for (int i = 1; i <= 21; i++) {
            BlockPos posS = posX.south(i);

            BlockState stateS = level.getBlockState(posS);
            if (!isEmpty.apply(stateS)) {
                if (!stateS.is(Blocks.OBSIDIAN)) {
                    return;
                }

                break;
            }

            if (!level.getBlockState(posS.west()).is(Blocks.OBSIDIAN)) {
                return;
            }

            distanceS++;
        }

        int distanceE = 1;
        for (int i = 1; i <= 21; i++) {
            BlockPos posE = posX.east(i);

            BlockState stateE = level.getBlockState(posE);
            if (!isEmpty.apply(stateE)) {
                if (!stateE.is(Blocks.OBSIDIAN)) {
                    return;
                }

                break;
            }

            if (!level.getBlockState(posE.north()).is(Blocks.OBSIDIAN)) {
                return;
            }

            distanceE++;
        }

        if ((distanceS < 3 && distanceE < 2) || (distanceS < 2 && distanceE < 3)) {
            return;
        }

        for (int x = 1; x < distanceE; x++) {
            for (int z = 1; z < distanceS; z++) {
                BlockPos posH = posX.offset(x, 0, z);

                if (x == distanceE - 1) {
                    if (!level.getBlockState(posH.east()).is(Blocks.OBSIDIAN)) {
                        return;
                    }
                }

                if (z == distanceS - 1) {
                    if (!level.getBlockState(posH.south()).is(Blocks.OBSIDIAN)) {
                        return;
                    }
                }

                if (!isEmpty.apply(level.getBlockState(posH))) {
                    return;
                }
            }
        }

        IServerLevel serverLevelX = (IServerLevel) level;
        PortalData portalData = serverLevelX.worldportal$getPortalData();
        Block block = Blocks.END_PORTAL;
        ResourceKey<Block> resourceKey = ResourceKey.create(
                Registries.BLOCK,
                BuiltInRegistries.BLOCK.getKey(block)
        );

        for (int x = 0; x < distanceE; x++) {
            for (int z = 0; z < distanceS; z++) {
                BlockPos posZ = posX.offset(x, 0, z);

                level.setBlock(posZ, block.defaultBlockState(), 18);

                portalData.putBlock(posZ, resourceKey);
            }
        }
    }
}
