package com.groupon.sthaleeya;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.groupon.sthaleeya.osm.Merchant;
import com.groupon.sthaleeya.osm.MerchantBusinessHours;
import com.groupon.sthaleeya.osm.OSMLoader;
import com.groupon.sthaleeya.osm.User;

public class GetAllMerchantsTask extends AsyncTask<Object, Void, Object[]> {
    private static final String TAG="GetAllMerchants";

    @Override
    protected Object[] doInBackground(Object... a) {
        ArrayList<Merchant> merchants = new ArrayList<Merchant>();
        List<User> friends=new ArrayList<User>();
        StringBuilder stringBuilder = null;
        try {
            HttpPost httppost;
            Log.i(TAG,a[0].toString());
            if(a[0].toString() != null) {
                httppost = new HttpPost(
                    Constants.SERVER_URL + "?category=ALL&id="+a[0].toString());
            } else { 
                httppost = new HttpPost(
                        Constants.SERVER_URL + "?category=ALL");
            }
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            stringBuilder = new StringBuilder();
            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            BufferedReader input=new BufferedReader(new InputStreamReader(stream));
            String b;
            while ((b = input.readLine()) != null) {
                stringBuilder.append( b);
            }

            // parse json data
            try {
                JSONObject jobject=new JSONObject(stringBuilder.toString());
                JSONArray merchantArray = jobject.getJSONArray("merchants");
                JSONArray friendsArray = jobject.getJSONArray("friends");
                for (int i = 0; i < merchantArray.length(); i++) {
                    JSONObject json_data = merchantArray.getJSONObject(i);
                    Merchant newMerchant = new Merchant();
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
                    merchants.add(newMerchant);
                }
                for (int i = 0; i < friendsArray.length(); i++) {
                    JSONObject json_data = friendsArray.getJSONObject(i);
                    User friend=new User();
                    friend.setId(json_data.getLong("id"));
                    friend.setLatitude(json_data.getString("latitude"));
                    friend.setLongitude(json_data.getString("longitude"));
                    friend.setName(json_data.getString("name"));
                    friend.setUpdated_time(json_data.getString("updated_time"));
                    friends.add(friend);
                }
                
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing data " + e.toString());
            }
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
        Object[] object=new Object[2];
        object[0]=merchants;
        object[1]=friends;
        return object;
    }

    @Override
    protected void onPostExecute(Object[] a) {
        super.onPostExecute(a);
        OSMLoader.osmloader.loadAll(a);
    }

}
