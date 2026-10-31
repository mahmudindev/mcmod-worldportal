package com.github.mahmudindev.mcmod.worldportal.portal;

import com.github.mahmudindev.mcmod.worldportal.WorldPortal;
import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public class PortalConfig {
    public static final Identifier DEFAULT_MODE = Identifier.tryBuild(
            WorldPortal.MOD_ID,
            "default"
    );
    public static final Identifier HORIZONTAL_MODE = Identifier.tryBuild(
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

    public Identifier getFrameC4(Direction.Axis axis) {
        Identifier frameTopRight = this.getFrameTopRightLocation();
        if (frameTopRight != null) {
            return frameTopRight;
        }

        if (axis == Direction.Axis.X) {
            Identifier frameTopEast = this.getFrameTopEastLocation();
            if (frameTopEast != null) {
                return frameTopEast;
            }

            return this.getFrameTopXCornerLocation();
        } else {
            Identifier frameTopSouth = this.getFrameTopSouthLocation();
            if (frameTopSouth != null) {
                return frameTopSouth;
            }

            return this.getFrameTopZCornerLocation();
        }
    }

    public String getFrameTopRight() {
        return this.frameTopRight;
    }

    public Identifier getFrameTopRightLocation() {
        String id = this.getFrameTopRight();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameTopRight(String frameTopRight) {
        this.frameTopRight = frameTopRight;
    }

    public String getFrameTopEast() {
        return this.frameTopEast;
    }

    public Identifier getFrameTopEastLocation() {
        String id = this.getFrameTopEast();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameTopEast(String frameTopEast) {
        this.frameTopEast = frameTopEast;
    }

    public String getFrameTopSouth() {
        return this.frameTopSouth;
    }

    public Identifier getFrameTopSouthLocation() {
        String id = this.getFrameTopSouth();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameTopSouth(String frameTopSouth) {
        this.frameTopSouth = frameTopSouth;
    }

    public Identifier getFrameC3(Direction.Axis axis) {
        Identifier frameTopLeft = this.getFrameTopLeftLocation();
        if (frameTopLeft != null) {
            return frameTopLeft;
        }

        if (axis == Direction.Axis.X) {
            Identifier frameTopWest = this.getFrameTopWestLocation();
            if (frameTopWest != null) {
                return frameTopWest;
            }

            return this.getFrameTopXCornerLocation();
        } else {
            Identifier frameTopNorth = this.getFrameTopNorthLocation();
            if (frameTopNorth != null) {
                return frameTopNorth;
            }

            return this.getFrameTopZCornerLocation();
        }
    }

    public String getFrameTopLeft() {
        return this.frameTopLeft;
    }

    public Identifier getFrameTopLeftLocation() {
        String id = this.getFrameTopLeft();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameTopLeft(String frameTopLeft) {
        this.frameTopLeft = frameTopLeft;
    }

    public String getFrameTopWest() {
        return this.frameTopWest;
    }

    public Identifier getFrameTopWestLocation() {
        String id = this.getFrameTopWest();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameTopWest(String frameTopWest) {
        this.frameTopWest = frameTopWest;
    }

    public String getFrameTopNorth() {
        return this.frameTopNorth;
    }

    public Identifier getFrameTopNorthLocation() {
        String id = this.getFrameTopNorth();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameTopNorth(String frameTopNorth) {
        this.frameTopNorth = frameTopNorth;
    }

    public String getFrameTopXCorner() {
        return this.frameTopXCorner;
    }

    public Identifier getFrameTopXCornerLocation() {
        String id = this.getFrameTopXCorner();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameTopXCorner(String frameTopXCorner) {
        this.frameTopXCorner = frameTopXCorner;
    }

    public String getFrameTopZCorner() {
        return this.frameTopZCorner;
    }

    public Identifier getFrameTopZCornerLocation() {
        String id = this.getFrameTopZCorner();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameTopZCorner(String frameTopZCorner) {
        this.frameTopZCorner = frameTopZCorner;
    }

    public Identifier getFrameC2(Direction.Axis axis) {
        Identifier frameBottomRight = this.getFrameBottomRightLocation();
        if (frameBottomRight != null) {
            return frameBottomRight;
        }

        if (axis == Direction.Axis.X) {
            Identifier frameBottomEast = this.getFrameBottomEastLocation();
            if (frameBottomEast != null) {
                return frameBottomEast;
            }

            return this.getFrameBottomXCornerLocation();
        } else {
            Identifier frameBottomSouth = this.getFrameBottomSouthLocation();
            if (frameBottomSouth != null) {
                return frameBottomSouth;
            }

            return this.getFrameBottomZCornerLocation();
        }
    }

    public String getFrameBottomRight() {
        return this.frameBottomRight;
    }

    public Identifier getFrameBottomRightLocation() {
        String id = this.getFrameBottomRight();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameBottomRight(String frameBottomRight) {
        this.frameBottomRight = frameBottomRight;
    }

    public String getFrameBottomEast() {
        return this.frameBottomEast;
    }

    public Identifier getFrameBottomEastLocation() {
        String id = this.getFrameBottomEast();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameBottomEast(String frameBottomEast) {
        this.frameBottomEast = frameBottomEast;
    }

    public String getFrameBottomSouth() {
        return this.frameBottomSouth;
    }

    public Identifier getFrameBottomSouthLocation() {
        String id = this.getFrameBottomSouth();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameBottomSouth(String frameBottomSouth) {
        this.frameBottomSouth = frameBottomSouth;
    }

    public Identifier getFrameC1(Direction.Axis axis) {
        Identifier frameBottomLeft = this.getFrameBottomLeftLocation();
        if (frameBottomLeft != null) {
            return frameBottomLeft;
        }

        if (axis == Direction.Axis.X) {
            Identifier frameBottomWest = this.getFrameBottomWestLocation();
            if (frameBottomWest != null) {
                return frameBottomWest;
            }

            return this.getFrameBottomXCornerLocation();
        } else {
            Identifier frameBottomNorth = this.getFrameBottomNorthLocation();
            if (frameBottomNorth != null) {
                return frameBottomNorth;
            }

            return this.getFrameBottomZCornerLocation();
        }
    }

    public String getFrameBottomLeft() {
        return this.frameBottomLeft;
    }

    public Identifier getFrameBottomLeftLocation() {
        String id = this.getFrameBottomLeft();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameBottomLeft(String frameBottomLeft) {
        this.frameBottomLeft = frameBottomLeft;
    }

    public String getFrameBottomWest() {
        return this.frameBottomWest;
    }

    public Identifier getFrameBottomWestLocation() {
        String id = this.getFrameBottomWest();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameBottomWest(String frameBottomWest) {
        this.frameBottomWest = frameBottomWest;
    }

    public String getFrameBottomNorth() {
        return this.frameBottomNorth;
    }

    public Identifier getFrameBottomNorthLocation() {
        String id = this.getFrameBottomNorth();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameBottomNorth(String frameBottomNorth) {
        this.frameBottomNorth = frameBottomNorth;
    }

    public String getFrameBottomXCorner() {
        return this.frameBottomXCorner;
    }

    public Identifier getFrameBottomXCornerLocation() {
        String id = this.getFrameBottomXCorner();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameBottomXCorner(String frameBottomXCorner) {
        this.frameBottomXCorner = frameBottomXCorner;
    }

    public String getFrameBottomZCorner() {
        return this.frameBottomZCorner;
    }

    public Identifier getFrameBottomZCornerLocation() {
        String id = this.getFrameBottomZCorner();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setFrameBottomZCorner(String frameBottomZCorner) {
        this.frameBottomZCorner = frameBottomZCorner;
    }

    public String getMode() {
        return this.mode;
    }

    public Identifier getModeLocation() {
        String id = this.getMode();

        if (id == null || id.isEmpty()) {
            return null;
        }

        return Identifier.parse(id);
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDestination() {
        return this.destination;
    }

    public Identifier getDestinationLocation() {
        return Identifier.parse(this.getDestination());
    }

    public ResourceKey<Level> getDestinationKey() {
        return ResourceKey.create(Registries.DIMENSION, this.getDestinationLocation());
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
