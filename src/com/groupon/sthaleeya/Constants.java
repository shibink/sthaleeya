package com.groupon.sthaleeya;


/**
 * Constant class
 */
public class Constants {

    // blocking the instance creation
    private Constants() {

    }

    // Fields specific to DB Table
    /**
     * name
     */
    public static final String CATEGORY = "category";
    public static final String _ID = "_id";
    public static final String MERCHANT_NAME = "name";
    public static final String BUSINESS_MERCHANT_ID = "merchant_id";
    
    /**
     * address
     */
    public static final String MERCHANT_ADDRESS = "address";
    
    /**
     * zip code
     */
    public static final String MERCHANT_ZIP = "zip_code";
    
    public static final String PHONE_NUM = "phone_no";

    /**
     * rating
     */
    public static final String MERCHANT_RATING = "rating";
    /**
     * latitude
     */
    public static final String LATITUDE = "latitude";

    /**
     * longitude
     */
    public static final String LONGITUDE = "longitude";

    /**
     * Database Name
     */
    public static final String OSM_DATABASE_NAME = "osm_db";
    public static final String DATABASE_NAME = "test";

    /**
     * Database Version
     */
    public static final int OSM_DATABASE_VERSION = 27;


    /**
     * DB table
     */
    public static final String MERCHANTS_TABLE = "merchants_table";

    public static final String ACTION_SETTINGS = "com.groupon.sthaleeya.action.settings";

    public static final String KEY_NAME = "key_name";
    public static final String KEY_DETAILS = "key_details";
    public static final String KEY_RADIUS = "key_radius";
    public static final String IS_MAP_VIEW = "is_map_view";
    /*
     * Time zone 
     */
    public static final String MERCHANT_ID="merchant_id";
    public static final String TIMEZONE= "timezone";
    public static final String BUSINESS_TIMINGS_TABLE="business_timings";
    public static final String BUSINESS_DAY="day";
    public static final String BUSINESS_OPEN_HR="openHr";
    public static final String BUSINESS_OPEN_MIN="openMin";
    public static final String BUSINESS_CLOSE_HR="closeHr";
    public static final String BUSINESS_CLOSE_MIN="closeMin";
    public static final String KEY_REFRESH_RATE = "refreshRate";
    
    public static final String SERVER_URL = "http://10.1.23.243/sthaleeya_all.php";
    public static final String ADD_USER_URL = "http://10.1.23.243/add_user.php";
    public static final String ADD_FRIENDS_URL = "http://10.1.23.243/add_friends.php";
    public static final String RETRIEVE_FRIENDS_URL = "http://10.1.23.243/retrieve_friends.php";
}
