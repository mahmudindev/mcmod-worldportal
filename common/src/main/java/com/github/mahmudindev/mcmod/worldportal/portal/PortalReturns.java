package com.github.mahmudindev.mcmod.worldportal.portal;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortalReturns extends SavedData {
    public static final Codec<PortalReturns> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                PortalReturn.CODEC.listOf().fieldOf("Dimensions").forGetter(v -> {
                    List<PortalReturn> portalReturns = new ArrayList<>();
                    v.dimensions.forEach((blockPos, resourceKey) -> {
                        portalReturns.add(new PortalReturn(blockPos, resourceKey));
                    });
                    return portalReturns;
                })
        ).apply(instance, v -> {
            Map<BlockPos, ResourceKey<Level>> dimensions = new HashMap<>();
            v.forEach(portalReturn -> dimensions.put(
                    portalReturn.blockPos,
                    portalReturn.resourceKey
            ));
            return new PortalReturns(dimensions);
        });
    });

    public static SavedDataType<PortalReturns> TYPE = new SavedDataType<>(
            WorldPortal.MOD_ID + "_returns",
            PortalReturns::new,
            CODEC,
            null
    );

    private final Map<BlockPos, ResourceKey<Level>> dimensions;

    public PortalReturns() {
        this.dimensions = new HashMap<>();
        this.setDirty();
    }

    private PortalReturns(Map<BlockPos, ResourceKey<Level>> dimensions) {
        this.dimensions = dimensions;
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

    record PortalReturn(BlockPos blockPos, ResourceKey<Level> resourceKey) {
        static final Codec<PortalReturn> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codec.INT.fieldOf("PosX").forGetter(v -> v.blockPos.getX()),
                    Codec.INT.fieldOf("PosY").forGetter(v -> v.blockPos.getY()),
                    Codec.INT.fieldOf("PosZ").forGetter(v -> v.blockPos.getZ()),
                    Identifier.CODEC.fieldOf("Dimension").forGetter(v -> {
                        return v.resourceKey.identifier();
                    })
            ).apply(instance, (x, y, z, identifier) -> new PortalReturn(
                    new BlockPos(x, y, z),
                    ResourceKey.create(Registries.DIMENSION, identifier)
            ));
        });
    }
}
