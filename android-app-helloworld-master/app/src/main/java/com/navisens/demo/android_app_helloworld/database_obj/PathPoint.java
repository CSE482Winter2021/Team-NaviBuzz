package com.navisens.demo.android_app_helloworld.database_obj;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity (tableName = "points", foreignKeys = {@ForeignKey(entity = Path.class, parentColumns = "path_id",
childColumns = "path_point_id")})
public class PathPoint {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "path_point_id")
    public int ppid;

    @ColumnInfo(name = "path_id")
    public int pid;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "instruction")
    public String instruction;

    @ColumnInfo(name = "landmark")
    public String landmark;

    public PathPoint(PathPoint other) {
        this.latitude = other.latitude;
        this.longitude = other.longitude;
        this.instruction = other.instruction;
        this.landmark = other.instruction;
        this.pid = other.pid;
        this.ppid = other.ppid;
    }

    public PathPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }
}
