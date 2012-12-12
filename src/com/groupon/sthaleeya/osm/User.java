package com.groupon.sthaleeya.osm;

public class User {
    private long id;
    private String name;
    private String latitude;
    private String longitude;
    private String updated_time;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLatitude() {
        return latitude;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    public String getLongitude() {
        return longitude;
    }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    public String getUpdatedTime() {
        return updated_time;
    }
    public void setUpdatedTime(String updated_time) {
        this.updated_time = updated_time;
    }
}
