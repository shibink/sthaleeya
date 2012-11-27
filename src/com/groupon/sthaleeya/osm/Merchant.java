package com.groupon.sthaleeya.osm;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.groupon.sthaleeya.Constants;
import com.groupon.sthaleeya.utils.CursorUtils;

public class Merchant {
    /** unique name identifier. */
    private long id;
    private String name;
    private String address;
    private String zipCode;
    private String phoneNumber;
    private double latitude;
    private double longitude;
    private double rating;
    private int timezone;
    private List<MerchantBusinessHours> businessHours = null;

    public Merchant() {

    }

    public Merchant(String name, String address, String zip, String phone, double rating,
            double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.zipCode = zip;
        this.phoneNumber = phone;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Merchant(String name, String address, String zip, String phone, double rating) {
        this.name = name;
        this.address = address;
        this.zipCode = zip;
        this.phoneNumber = phone;
        this.rating = rating;

    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setBusinessHours(ArrayList<MerchantBusinessHours> businessHours) {
        this.businessHours = businessHours;
    }

    public List<MerchantBusinessHours> getBusinessHours() {
        return this.businessHours;
    }

    public void setTimezone(int zone) {
        this.timezone = zone;
    }

    public int getTimezone() {
        return this.timezone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setZip(String zip) {
        zipCode = zip;
    }

    public void setPhoneNumber(String phone) {
        phoneNumber = phone;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getZip() {
        return zipCode;
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

    public double getRating() {
        return rating;
    }

    /**
     * Creates merchant object
     */
    public static Merchant getMerchant(String str) {
        final Merchant merchant = new Merchant();

        return merchant;
    }

    /**
     * To get ad unit from cursor
     * 
     * @param cursor
     * @return
     */
    public static Merchant getMerchantFromCursor(Cursor cursor) {
        if ((cursor == null) || (cursor.getCount() <= 0)) {
            return null;
        }

        Merchant merchant = new Merchant();

        // populate ads table data
        merchant.id = CursorUtils.getLongFromCursor(Constants._ID, cursor);
        merchant.name = CursorUtils.getStringFromCursor(Constants.MERCHANT_NAME, cursor);
        merchant.address = CursorUtils.getStringFromCursor(Constants.MERCHANT_ADDRESS,
                cursor);
        merchant.zipCode = CursorUtils
                .getStringFromCursor(Constants.MERCHANT_ZIP, cursor);
        merchant.latitude = CursorUtils.getDoubleFromCursor(Constants.LATITUDE, cursor);
        merchant.longitude = CursorUtils.getDoubleFromCursor(Constants.LONGITUDE, cursor);
        merchant.phoneNumber = CursorUtils.getStringFromCursor(Constants.PHONE_NUM,
                cursor);

        return merchant;
    }
}
