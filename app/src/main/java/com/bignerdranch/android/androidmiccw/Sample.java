package com.bignerdranch.android.androidmiccw;

public class Sample {

    private String uid;
    private String iid;
    private long startTime;
    private long stopTime;
    private double latitude;
    private double longitude;
    private int maxDecibels;

    public Sample() {}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
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

    public int getMaxDecibels() {
        return maxDecibels;
    }

    public void setMaxDecibels(int maxDecibels) {
        this.maxDecibels = maxDecibels;
    }
}
