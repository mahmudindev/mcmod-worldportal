package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import net.minecraft.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random,
            CallbackInfoReturnable<BlockState> cir
    ) {
        if (!state.is(Blocks.END_PORTAL)) {
            return;
        }

        if (!(level instanceof LevelAccessor levelAccessor)) {
            return;
        }

        BlockUtil.FoundRectangle largestRectangleAround = BlockUtil.getLargestRectangleAround(
                pos,
                Direction.Axis.X,
                21,
                Direction.Axis.Z,
                21,
                posX -> levelAccessor.getBlockState(posX) == state
        );

        boolean isComplete = true, isCustom = false;

        for (int x = 0; x < largestRectangleAround.axis1Size; x++) {
            for (int z = 0; z < largestRectangleAround.axis2Size; z++) {
                BlockPos posH = largestRectangleAround.minCorner.offset(x, 0, z);

                for (Map.Entry<BlockPos, Boolean> entry : Map.of(
                        posH.west(), x == 0,
                        posH.east(), x == largestRectangleAround.axis1Size - 1,
                        posH.north(), z == 0,
                        posH.south(), z == largestRectangleAround.axis2Size - 1
                ).entrySet()) {
                    if (!entry.getValue()) {
                        continue;
                    }

                    BlockState stateX = levelAccessor.getBlockState(entry.getKey());

                    if (stateX.isAir()) {
                        isComplete = false;
                    }

                    if (stateX.is(Blocks.OBSIDIAN)) {
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

        if (!(levelAccessor instanceof ServerLevel)) {
            return;
        }

        IServerLevel serverLevelX = (IServerLevel) levelAccessor;
        PortalData portalData = serverLevelX.worldportal$getPortalData();

        for (int x = 0; x < largestRectangleAround.axis1Size; x++) {
            for (int z = 0; z < largestRectangleAround.axis2Size; z++) {
                BlockPos posX = largestRectangleAround.minCorner.offset(x, 0, z);

                levelAccessor.setBlock(posX, Blocks.AIR.defaultBlockState(), 18);

                portalData.removeBlock(posX);
            }
        }

        cir.setReturnValue(Blocks.AIR.defaultBlockState());
    }
}
