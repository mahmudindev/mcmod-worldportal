package com.github.mahmudindev.mcmod.worldportal.portal;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortalData extends SavedData {
    public static final Codec<PortalData> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                PortalBlock.CODEC.listOf().fieldOf("Blocks").forGetter(v -> {
                    List<PortalBlock> portalBlocks = new ArrayList<>();
                    v.blocks.forEach((blockPos, resourceKey) -> {
                        portalBlocks.add(new PortalBlock(blockPos, resourceKey));
                    });
                    return portalBlocks;
                }),
                PortalDimension.CODEC.listOf().fieldOf("Dimensions").forGetter(v -> {
                    List<PortalDimension> portalDimensions = new ArrayList<>();
                    v.dimensions.forEach((blockPos, resourceKey) -> {
                        portalDimensions.add(new PortalDimension(blockPos, resourceKey));
                    });
                    return portalDimensions;
                })
        ).apply(instance, (portalBlocks, portalDimensions) -> {
            Map<BlockPos, ResourceKey<Block>> blocks = new HashMap<>();
            portalBlocks.forEach(portalBlock -> blocks.put(
                    portalBlock.blockPos,
                    portalBlock.resourceKey
            ));
            Map<BlockPos, ResourceKey<Level>> dimensions = new HashMap<>();
            portalDimensions.forEach(portalBlock -> dimensions.put(
                    portalBlock.blockPos,
                    portalBlock.resourceKey
            ));
            return new PortalData(blocks, dimensions);
        });
    });

    public static SavedDataType<PortalData> TYPE = new SavedDataType<>(
            WorldPortal.MOD_ID + "_data",
            PortalData::new,
            CODEC,
            null
    );

    private final Map<BlockPos, ResourceKey<Block>> blocks;
    private final Map<BlockPos, ResourceKey<Level>> dimensions;

    public PortalData() {
        this.blocks = new HashMap<>();
        this.dimensions = new HashMap<>();
        this.setDirty();
    }

    private PortalData(
            Map<BlockPos, ResourceKey<Block>> blocks,
            Map<BlockPos, ResourceKey<Level>> dimensions
    ) {
        this.blocks = blocks;
        this.dimensions = dimensions;
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

    public Map<BlockPos, ResourceKey<Level>> getDimensions() {
        return Map.copyOf(this.dimensions);
    }

    public ResourceKey<Level> getDimension(BlockPos pos) {
        return this.dimensions.get(pos);
    }

    public void putDimension(BlockPos pos, ResourceKey<Level> dimension) {
        this.dimensions.put(pos, dimension);
        this.setDirty();
    }

    public void removeDimension(BlockPos pos) {
        this.dimensions.remove(pos);
        this.setDirty();
    }

    record PortalBlock(BlockPos blockPos, ResourceKey<Block> resourceKey) {
        static final Codec<PortalBlock> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codec.INT.fieldOf("PosX").forGetter(v -> v.blockPos.getX()),
                    Codec.INT.fieldOf("PosY").forGetter(v -> v.blockPos.getY()),
                    Codec.INT.fieldOf("PosZ").forGetter(v -> v.blockPos.getZ()),
                    ResourceLocation.CODEC.fieldOf("Block").forGetter(v -> {
                        return v.resourceKey.location();
                    })
            ).apply(instance, (x, y, z, resourceLocation) -> new PortalBlock(
                    new BlockPos(x, y, z),
                    ResourceKey.create(Registries.BLOCK, resourceLocation)
            ));
        });
    }

    record PortalDimension(BlockPos blockPos, ResourceKey<Level> resourceKey) {
        static final Codec<PortalData.PortalDimension> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codec.INT.fieldOf("PosX").forGetter(v -> v.blockPos.getX()),
                    Codec.INT.fieldOf("PosY").forGetter(v -> v.blockPos.getY()),
                    Codec.INT.fieldOf("PosZ").forGetter(v -> v.blockPos.getZ()),
                    ResourceLocation.CODEC.fieldOf("Dimension").forGetter(v -> {
                        return v.resourceKey.location();
                    })
            ).apply(instance, (x, y, z, resourceLocation) -> new PortalData.PortalDimension(
                    new BlockPos(x, y, z),
                    ResourceKey.create(Registries.DIMENSION, resourceLocation)
            ));
        });
    }
}
