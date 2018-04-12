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

    /**
     * Uid is the user ID from Firebase
     */
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Iid is the instance ID, a unique identifier for each app instance.
     * From Google Play Services.
     */
    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    /**
     * Time of start of recording, taken from Date().getTime().
     */
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Time of stopping recording, taken from Date().getTime().
     */
    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    /**
     * Gives latitude in degrees north.
     */
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Longitude in degrees east.
     */
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * The loudest sound made during the duration of a recording, in decibels.
     */
    public int getMaxDecibels() {
        return maxDecibels;
    }

    public void setMaxDecibels(int maxDecibels) {
        this.maxDecibels = maxDecibels;
    }
}
