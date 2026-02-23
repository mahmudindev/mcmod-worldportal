package com.github.mahmudindev.mcmod.worldportal.mixin;

import com.github.mahmudindev.mcmod.worldportal.base.IServerLevel;
import com.github.mahmudindev.mcmod.worldportal.portal.PortalData;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
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
            Direction direction,
            BlockState blockState2,
            LevelAccessor levelAccessor,
            BlockPos blockPos,
            BlockPos blockPos2,
            CallbackInfoReturnable<BlockState> cir
    ) {
        if (!blockState.is(Blocks.END_PORTAL)) {
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

        if (!(levelAccessor instanceof ServerLevel)) {
            return;
        }

        IServerLevel serverLevelX = (IServerLevel) levelAccessor;
        PortalData portalData = serverLevelX.worldportal$getPortalData();

        for (int x = 0; x < foundRectangle.axis1Size; x++) {
            for (int z = 0; z < foundRectangle.axis2Size; z++) {
                BlockPos blockPosX = foundRectangle.minCorner.offset(x, 0, z);

                levelAccessor.setBlock(blockPosX, Blocks.AIR.defaultBlockState(), 18);

                portalData.removeBlock(blockPosX);
            }
        }

        cir.setReturnValue(Blocks.AIR.defaultBlockState());
    }
}
