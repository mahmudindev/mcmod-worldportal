package com.github.mahmudindev.mcmod.worldportal.portal;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class PortalConfig {
    public static final ResourceLocation DEFAULT_MODE = new ResourceLocation(
            WorldPortal.MOD_ID,
            "default"
    );
    public static final ResourceLocation HORIZONTAL_MODE = new ResourceLocation(
            WorldPortal.MOD_ID,
            "horizontal"
    );

    @SerializedName("frame_top_right")
    private String frameTopRight;
    @SerializedName("frame_top_east")
    private String frameTopEast;
    @SerializedName("frame_top_south")
    private String frameTopSouth;
    @SerializedName("frame_top_left")
    private String frameTopLeft;
    @SerializedName("frame_top_west")
    private String frameTopWest;
    @SerializedName("frame_top_north")
    private String frameTopNorth;
    @SerializedName("frame_top_x_corner")
    private String frameTopXCorner;
    @SerializedName("frame_top_z_corner")
    private String frameTopZCorner;
    @SerializedName("frame_bottom_right")
    private String frameBottomRight;
    @SerializedName("frame_bottom_east")
    private String frameBottomEast;
    @SerializedName("frame_bottom_south")
    private String frameBottomSouth;
    @SerializedName("frame_bottom_left")
    private String frameBottomLeft;
    @SerializedName("frame_bottom_west")
    private String frameBottomWest;
    @SerializedName("frame_bottom_north")
    private String frameBottomNorth;
    @SerializedName("frame_bottom_x_corner")
    private String frameBottomXCorner;
    @SerializedName("frame_bottom_z_corner")
    private String frameBottomZCorner;
    private String mode;
    private String destination;

    public ResourceLocation getFrameC4(Direction.Axis axis) {
        ResourceLocation frameTopRight = this.getFrameTopRightLocation();
        if (frameTopRight != null) {
            return frameTopRight;
        }

        if (axis == Direction.Axis.X) {
            ResourceLocation frameTopEast = this.getFrameTopEastLocation();
            if (frameTopEast != null) {
                return frameTopEast;
            }

            return this.getFrameTopXCornerLocation();
        } else {
            ResourceLocation frameTopSouth = this.getFrameTopSouthLocation();
            if (frameTopSouth != null) {
                return frameTopSouth;
            }

            return this.getFrameTopZCornerLocation();
        }
    }

    public String getFrameTopRight() {
        return this.frameTopRight;
    }

    public ResourceLocation getFrameTopRightLocation() {
        String id = this.getFrameTopRight();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameTopRight(String frameTopRight) {
        this.frameTopRight = frameTopRight;
    }

    public String getFrameTopEast() {
        return this.frameTopEast;
    }

    public ResourceLocation getFrameTopEastLocation() {
        String id = this.getFrameTopEast();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameTopEast(String frameTopEast) {
        this.frameTopEast = frameTopEast;
    }

    public String getFrameTopSouth() {
        return this.frameTopSouth;
    }

    public ResourceLocation getFrameTopSouthLocation() {
        String id = this.getFrameTopSouth();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameTopSouth(String frameTopSouth) {
        this.frameTopSouth = frameTopSouth;
    }

    public ResourceLocation getFrameC3(Direction.Axis axis) {
        ResourceLocation frameTopLeft = this.getFrameTopLeftLocation();
        if (frameTopLeft != null) {
            return frameTopLeft;
        }

        if (axis == Direction.Axis.X) {
            ResourceLocation frameTopWest = this.getFrameTopWestLocation();
            if (frameTopWest != null) {
                return frameTopWest;
            }

            return this.getFrameTopXCornerLocation();
        } else {
            ResourceLocation frameTopNorth = this.getFrameTopNorthLocation();
            if (frameTopNorth != null) {
                return frameTopNorth;
            }

            return this.getFrameTopZCornerLocation();
        }
    }

    public String getFrameTopLeft() {
        return this.frameTopLeft;
    }

    public ResourceLocation getFrameTopLeftLocation() {
        String id = this.getFrameTopLeft();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameTopLeft(String frameTopLeft) {
        this.frameTopLeft = frameTopLeft;
    }

    public String getFrameTopWest() {
        return this.frameTopWest;
    }

    public ResourceLocation getFrameTopWestLocation() {
        String id = this.getFrameTopWest();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameTopWest(String frameTopWest) {
        this.frameTopWest = frameTopWest;
    }

    public String getFrameTopNorth() {
        return this.frameTopNorth;
    }

    public ResourceLocation getFrameTopNorthLocation() {
        String id = this.getFrameTopNorth();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameTopNorth(String frameTopNorth) {
        this.frameTopNorth = frameTopNorth;
    }

    public String getFrameTopXCorner() {
        return this.frameTopXCorner;
    }

    public ResourceLocation getFrameTopXCornerLocation() {
        String id = this.getFrameTopXCorner();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameTopXCorner(String frameTopXCorner) {
        this.frameTopXCorner = frameTopXCorner;
    }

    public String getFrameTopZCorner() {
        return this.frameTopZCorner;
    }

    public ResourceLocation getFrameTopZCornerLocation() {
        String id = this.getFrameTopZCorner();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameTopZCorner(String frameTopZCorner) {
        this.frameTopZCorner = frameTopZCorner;
    }

    public ResourceLocation getFrameC2(Direction.Axis axis) {
        ResourceLocation frameBottomRight = this.getFrameBottomRightLocation();
        if (frameBottomRight != null) {
            return frameBottomRight;
        }

        if (axis == Direction.Axis.X) {
            ResourceLocation frameBottomEast = this.getFrameBottomEastLocation();
            if (frameBottomEast != null) {
                return frameBottomEast;
            }

            return this.getFrameBottomXCornerLocation();
        } else {
            ResourceLocation frameBottomSouth = this.getFrameBottomSouthLocation();
            if (frameBottomSouth != null) {
                return frameBottomSouth;
            }

            return this.getFrameBottomZCornerLocation();
        }
    }

    public String getFrameBottomRight() {
        return this.frameBottomRight;
    }

    public ResourceLocation getFrameBottomRightLocation() {
        String id = this.getFrameBottomRight();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameBottomRight(String frameBottomRight) {
        this.frameBottomRight = frameBottomRight;
    }

    public String getFrameBottomEast() {
        return this.frameBottomEast;
    }

    public ResourceLocation getFrameBottomEastLocation() {
        String id = this.getFrameBottomEast();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameBottomEast(String frameBottomEast) {
        this.frameBottomEast = frameBottomEast;
    }

    public String getFrameBottomSouth() {
        return this.frameBottomSouth;
    }

    public ResourceLocation getFrameBottomSouthLocation() {
        String id = this.getFrameBottomSouth();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameBottomSouth(String frameBottomSouth) {
        this.frameBottomSouth = frameBottomSouth;
    }

    public ResourceLocation getFrameC1(Direction.Axis axis) {
        ResourceLocation frameBottomLeft = this.getFrameBottomLeftLocation();
        if (frameBottomLeft != null) {
            return frameBottomLeft;
        }

        if (axis == Direction.Axis.X) {
            ResourceLocation frameBottomWest = this.getFrameBottomWestLocation();
            if (frameBottomWest != null) {
                return frameBottomWest;
            }

            return this.getFrameBottomXCornerLocation();
        } else {
            ResourceLocation frameBottomNorth = this.getFrameBottomNorthLocation();
            if (frameBottomNorth != null) {
                return frameBottomNorth;
            }

            return this.getFrameBottomZCornerLocation();
        }
    }

    public String getFrameBottomLeft() {
        return this.frameBottomLeft;
    }

    public ResourceLocation getFrameBottomLeftLocation() {
        String id = this.getFrameBottomLeft();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameBottomLeft(String frameBottomLeft) {
        this.frameBottomLeft = frameBottomLeft;
    }

    public String getFrameBottomWest() {
        return this.frameBottomWest;
    }

    public ResourceLocation getFrameBottomWestLocation() {
        String id = this.getFrameBottomWest();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameBottomWest(String frameBottomWest) {
        this.frameBottomWest = frameBottomWest;
    }

    public String getFrameBottomNorth() {
        return this.frameBottomNorth;
    }

    public ResourceLocation getFrameBottomNorthLocation() {
        String id = this.getFrameBottomNorth();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameBottomNorth(String frameBottomNorth) {
        this.frameBottomNorth = frameBottomNorth;
    }

    public String getFrameBottomXCorner() {
        return this.frameBottomXCorner;
    }

    public ResourceLocation getFrameBottomXCornerLocation() {
        String id = this.getFrameBottomXCorner();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameBottomXCorner(String frameBottomXCorner) {
        this.frameBottomXCorner = frameBottomXCorner;
    }

    public String getFrameBottomZCorner() {
        return this.frameBottomZCorner;
    }

    public ResourceLocation getFrameBottomZCornerLocation() {
        String id = this.getFrameBottomZCorner();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setFrameBottomZCorner(String frameBottomZCorner) {
        this.frameBottomZCorner = frameBottomZCorner;
    }

    public String getMode() {
        return this.mode;
    }

    public ResourceLocation getModeLocation() {
        String id = this.getMode();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return new ResourceLocation(id);
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDestination() {
        return this.destination;
    }

    public ResourceLocation getDestinationLocation() {
        return new ResourceLocation(this.getDestination());
    }

    public ResourceKey<Level> getDestinationKey() {
        return ResourceKey.create(Registries.DIMENSION, this.getDestinationLocation());
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
