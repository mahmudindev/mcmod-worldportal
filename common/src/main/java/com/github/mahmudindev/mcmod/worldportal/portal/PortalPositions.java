package com.github.mahmudindev.mcmod.worldportal.portal;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortalPositions extends SavedData {
    public static final Codec<PortalPositions> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                PortalPositions.PortalPosition.CODEC.listOf().fieldOf("Blocks").forGetter(v -> {
                    List<PortalPositions.PortalPosition> portalPositions = new ArrayList<>();
                    v.blocks.forEach((blockPos, resourceKey) -> {
                        portalPositions.add(new PortalPositions.PortalPosition(blockPos, resourceKey));
                    });
                    return portalPositions;
                })
        ).apply(instance, v -> {
            Map<BlockPos, ResourceKey<Block>> blocks = new HashMap<>();
            v.forEach(portalPosition -> blocks.put(
                    portalPosition.blockPos,
                    portalPosition.resourceKey
            ));
            return new PortalPositions(blocks);
        });
    });

    public static SavedDataType<PortalPositions> TYPE = new SavedDataType<>(
            WorldPortal.MOD_ID + "_positions",
            PortalPositions::new,
            CODEC,
            null
    );

    private final Map<BlockPos, ResourceKey<Block>> blocks;

    public PortalPositions() {
        this.blocks = new HashMap<>();
        this.setDirty();
    }

    private PortalPositions(Map<BlockPos, ResourceKey<Block>> dimensions) {
        this.blocks = dimensions;
    }

    public Map<BlockPos, ResourceKey<Block>> getBlocks() {
        return Map.copyOf(this.blocks);
    }

    public ResourceKey<Block> getBlock(BlockPos pos) {
        return this.blocks.get(pos);
    }

    public void putBlock(BlockPos pos, ResourceKey<Block> block) {
        this.blocks.put(pos, block);
        this.setDirty();
    }

    public void removeBlock(BlockPos pos) {
        this.blocks.remove(pos);
        this.setDirty();
    }

    record PortalPosition(BlockPos blockPos, ResourceKey<Block> resourceKey) {
        static final Codec<PortalPositions.PortalPosition> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codec.INT.fieldOf("PosX").forGetter(v -> v.blockPos.getX()),
                    Codec.INT.fieldOf("PosY").forGetter(v -> v.blockPos.getY()),
                    Codec.INT.fieldOf("PosZ").forGetter(v -> v.blockPos.getZ()),
                    ResourceLocation.CODEC.fieldOf("Block").forGetter(v -> {
                        return v.resourceKey.location();
                    })
            ).apply(instance, (x, y, z, resourceLocation) -> new PortalPositions.PortalPosition(
                    new BlockPos(x, y, z),
                    ResourceKey.create(Registries.BLOCK, resourceLocation)
            ));
        });
    }
}
