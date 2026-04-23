package com.github.mahmudindev.mcmod.worldportal.mixin;

import net.minecraft.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(EndPortalBlock.class)
public abstract class EndPortalBlockMixin {
    @Inject(
            method = "entityInside",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;"
            ),
            cancellable = true
    )
    private void entityInsidePrepare(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity,
            InsideBlockEffectApplier effectApplier,
            boolean isPrecise,
            CallbackInfo ci
    ) {
        if (entity.isOnPortalCooldown()) {
            entity.setPortalCooldown();

            ci.cancel();
            return;
        }

        BlockUtil.FoundRectangle largestRectangleAround = BlockUtil.getLargestRectangleAround(
                pos,
                Direction.Axis.X,
                21,
                Direction.Axis.Z,
                21,
                posX -> level.getBlockState(posX) == state
        );

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

                    if (!level.getBlockState(entry.getKey()).is(Blocks.OBSIDIAN)) {
                        return;
                    }
                }
            }
        }

        entity.setAsInsidePortal((Portal) Blocks.NETHER_PORTAL, pos);
        ci.cancel();
    }
}
