package com.groupon.sthaleeya.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class LocationUtil {
    // Should be same as radius_values.xml
    private static int[] radiusValues = {1,2,5,10,15,20,30,40,50};
    private static int[] zoomLevels = {15,14,12,10,9,9,9,9,8};
    public static final int DEFAULT_ZOOM = 8;

    public static Location getLocation(String address) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            address = address.replaceAll(" ", "%20");

            HttpPost httppost = new HttpPost(
                    "http://maps.google.com/maps/api/geocode/json?address=" + address + "&sensor=false");
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            stringBuilder = new StringBuilder();

            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.e("LocationUtil", "Error getting location: ", e);
            return null;
        }
        GeoPoint point = getGeoPoint(jsonObject);
        
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setLatitude(point.getLatitudeE6()/1000000.0);
        loc.setLongitude(point.getLongitudeE6()/1000000.0);
        return loc;
    }
    
    private static GeoPoint getGeoPoint(JSONObject object) {
        Double lon = 0.0;
        Double lat = 0.0;

        try {
            lon = ((JSONArray)object.get("results")).getJSONObject(0)
                .getJSONObject("geometry").getJSONObject("location")
                .getDouble("lng");

            lat = ((JSONArray)object.get("results")).getJSONObject(0)
                .getJSONObject("geometry").getJSONObject("location")
                .getDouble("lat");
        } catch (Exception e) {
            e.printStackTrace();

        }

        return new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));
    }
    
    public static int getZoomLevel(int radius) {
        return zoomLevels[getIndexOf(radius)];
    }
    
    public static int getIndexOf(int radius) {
        for (int i=0; i<radiusValues.length; i++) {
            if (radiusValues[i] == radius) {
                return i;
            }
        }

        return DEFAULT_ZOOM;
    }
}
