package com.groupon.sthaleeya;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.groupon.sthaleeya.osm.Merchant;
import com.groupon.sthaleeya.osm.MerchantBusinessHours;
import com.groupon.sthaleeya.osm.OSMLoader;

public class GetDetailsOfMerchant extends AsyncTask<Object, Void, Object[]> {
    private static final String TAG = "GetDetailsOfMerchant";

    @Override
    protected Object[] doInBackground(Object... params) {
        
        Long id = (Long) params[1];
        Merchant newMerchant = null;
        StringBuilder stringBuilder = null;
        try {
            HttpPost httppost = new HttpPost(Constants.SERVER_URL + "?id=" + id
                    + "&type=2");
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
            Log.i(TAG, stringBuilder.toString());
            // parse json data
            try {
                JSONObject jArray = new JSONObject(stringBuilder.toString());
                JSONArray json = jArray.getJSONArray("merchants");
                JSONObject json_data=json.getJSONObject(0);
                
                newMerchant = new Merchant();
                MerchantBusinessHours businessHours = new MerchantBusinessHours();
                businessHours.setDay(json_data.getString("day"));
                businessHours.setOpenHr(json_data.getInt("openHr"));
                businessHours.setOpenMin(json_data.getInt("openMin"));
                businessHours.setCloseHr(json_data.getInt("closeHr"));
                businessHours.setCloseMin(json_data.getInt("closeMin"));
                newMerchant.setId(json_data.getInt("_id"));
                newMerchant.setAddress(json_data.getString("address"));
                newMerchant.setName(json_data.getString("name"));
                newMerchant.setZip(json_data.getString("zip_code"));
                newMerchant.setPhoneNumber(json_data.getString("phone_no"));
                newMerchant.setRating(json_data.getDouble("rating"));
                newMerchant.setTimezone(json_data.getString("timezone"));
                newMerchant.setLatitude(json_data.getDouble("latitude"));
                newMerchant.setLongitude(json_data.getDouble("longitude"));
                ArrayList<MerchantBusinessHours> businessArray = new ArrayList<MerchantBusinessHours>();
                businessArray.add(businessHours);
                newMerchant.setBusinessHours(businessArray);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing data " + e.toString());
            }
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
        Object[] returnObjects = new Object[2];
        returnObjects[0] = newMerchant;
        returnObjects[1] = params[2];
        return returnObjects;
    }

    @Override
    protected void onPostExecute(Object[] objects) {
        super.onPostExecute(objects);
        OSMLoader.osmloader.showMerchantOnTap((Merchant) objects[0], (Bundle) objects[1]);
    }

}
