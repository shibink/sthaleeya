package com.groupon.sthaleeya.users;

import java.util.List;

public class User {
    private long id;
    private String name;
    private String email;
    private String address;
    private String phoneNumber;
    private List<User> friends;
    
    private double latitude;
    private double longitude;

    public User() {

    }

    public User(String name, String email, String address, String phone) {
        this.name = name;
        this.address = address;
        this.email = email;
        this.phoneNumber = phone;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    public List<User> getFriends() {
        return this.friends;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phone) {
        phoneNumber = phone;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
