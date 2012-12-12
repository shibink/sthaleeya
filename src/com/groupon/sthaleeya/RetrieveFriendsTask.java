package com.groupon.sthaleeya;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.groupon.sthaleeya.osm.OSMLoader;
import com.groupon.sthaleeya.osm.User;

import android.os.AsyncTask;
import android.util.Log;

public class RetrieveFriendsTask extends AsyncTask<Object, Void, List<User>> {
    private static final String TAG="RetrieveFriendsTask";

    @Override
    protected List<User> doInBackground(Object... a) {
        List<User> friends=new ArrayList<User>();
        try {
            HttpPost httppost = new HttpPost(
                    Constants.RETRIEVE_FRIENDS_URL );
            HttpClient httpclient = new DefaultHttpClient();
            StringBuilder stringBuilder = null;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
                nameValuePairs.add(new BasicNameValuePair("id", a[0].toString()));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                httpclient.execute(httppost);
                HttpResponse response;
                stringBuilder = new StringBuilder();
                response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }

                // parse json data
                try {
                    JSONArray jArray = new JSONArray(stringBuilder.toString());
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        User friend=new User();
                        friend.setId(json_data.getLong("id"));
                        friend.setLatitude(json_data.getString("latitude"));
                        friend.setLongitude(json_data.getString("longitude"));
                        friend.setName(json_data.getString("name"));
                        friend.setUpdatedTime(json_data.getString("updated_time"));
                        friends.add(friend);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing data " + e.toString());
                }
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
        return friends;
   }
    @Override
    protected void onPostExecute(List<User> a) {
        super.onPostExecute(a);
       // OSMLoader.osmloader.loadFriends(a);
    }
}
