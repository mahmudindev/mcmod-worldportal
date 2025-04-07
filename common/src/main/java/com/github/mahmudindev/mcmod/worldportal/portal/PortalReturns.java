package com.github.mahmudindev.mcmod.worldportal.portal;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class PortalReturns extends SavedData {
    public static String FIELD = WorldPortal.MOD_ID + "_returns";

    private final Map<BlockPos, ResourceKey<Level>> dimensions = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        this.dimensions.forEach((k, v) -> {
            CompoundTag compoundTagX = new CompoundTag();
            compoundTagX.putInt("PosX", k.getX());
            compoundTagX.putInt("PosY", k.getY());
            compoundTagX.putInt("PosZ", k.getZ());
            compoundTagX.putString("Dimension", String.valueOf(v.location()));

            listTag.add(compoundTagX);
        });
        compoundTag.put("Dimensions", listTag);

        return compoundTag;
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

    public static SavedData.Factory<PortalReturns> factory() {
        return new SavedData.Factory<>(
                PortalReturns::new,
                (compoundTag, provider) -> load(compoundTag),
                null
        );
    }

    public static PortalReturns load(CompoundTag compoundTag) {
        PortalReturns portalReturns = new PortalReturns();

        ListTag dimensionsTag = compoundTag.getList("Dimensions", 10);
        for(int i = 0; i < dimensionsTag.size(); ++i) {
            CompoundTag compoundTagX = dimensionsTag.getCompound(i);
            portalReturns.dimensions.put(
                    new BlockPos(
                            compoundTagX.getInt("PosX"),
                            compoundTagX.getInt("PosY"),
                            compoundTagX.getInt("PosZ")
                    ),
                    ResourceKey.create(
                            Registries.DIMENSION,
                            ResourceLocation.parse(compoundTagX.getString("Dimension"))
                    )
            );
        }

        return portalReturns;
    }
}
