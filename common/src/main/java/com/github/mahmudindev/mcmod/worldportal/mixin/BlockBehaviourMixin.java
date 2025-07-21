package com.github.mahmudindev.mcmod.worldportal.mixin;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    @Inject(method = "updateShape", at = @At("TAIL"), cancellable = true)
    private void updateShapeHorizontalPortal(
            BlockState blockState,
            LevelReader levelReader,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos blockPos,
            Direction direction,
            BlockPos blockPos2,
            BlockState blockState2,
            RandomSource randomSource,
            CallbackInfoReturnable<BlockState> cir
    ) {
        if (!blockState.is(Blocks.END_PORTAL)) {
            return;
        }

        if (!(levelReader instanceof LevelAccessor levelAccessor)) {
            return;
        }

        BlockUtil.FoundRectangle foundRectangle = BlockUtil.getLargestRectangleAround(
                blockPos,
                Direction.Axis.X,
                21,
                Direction.Axis.Z,
                21,
                blockPosX -> levelAccessor.getBlockState(blockPosX) == blockState
        );

        boolean isComplete = true, isCustom = false;

        for (int x = 0; x < foundRectangle.axis1Size; x++) {
            for (int z = 0; z < foundRectangle.axis2Size; z++) {
                BlockPos blockPosH = foundRectangle.minCorner.offset(x, 0, z);

                for (Map.Entry<BlockPos, Boolean> entry : Map.of(
                        blockPosH.west(), x == 0,
                        blockPosH.east(), x == foundRectangle.axis1Size - 1,
                        blockPosH.north(), z == 0,
                        blockPosH.south(), z == foundRectangle.axis2Size - 1
                ).entrySet()) {
                    if (!entry.getValue()) {
                        continue;
                    }

                    BlockState blockStateX = levelAccessor.getBlockState(entry.getKey());

                    if (blockStateX.isAir()) {
                        isComplete = false;
                    }

                    if (blockStateX.is(Blocks.OBSIDIAN)) {
                        isCustom = true;
                    }
                }
            }

            if (!isComplete && isCustom) {
                break;
            }
        }

        if (isComplete || !isCustom) {
            return;
        }

        for (int x = 0; x < foundRectangle.axis1Size; x++) {
            for (int z = 0; z < foundRectangle.axis2Size; z++) {
                levelAccessor.setBlock(
                        foundRectangle.minCorner.offset(x, 0, z),
                        Blocks.AIR.defaultBlockState(),
                        18
                );
            }
        }

        cir.setReturnValue(Blocks.AIR.defaultBlockState());
    }
}
