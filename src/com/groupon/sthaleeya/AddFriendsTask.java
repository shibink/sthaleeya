package com.groupon.sthaleeya;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.groupon.sthaleeya.osm.OSMLoader;

import android.os.AsyncTask;
import android.util.Log;

public class AddFriendsTask extends AsyncTask<Object, Void, Void> {
    private static final String TAG="AddFriendsTask";

    @Override
    protected Void doInBackground(Object... a) {
        try {
            HttpPost httppost = new HttpPost(
                    Constants.ADD_Friends_URL );
            HttpClient httpclient = new DefaultHttpClient();
            
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
                nameValuePairs.add(new BasicNameValuePair("id", a[0].toString()));
                String[] friends=(String[]) a[1];
                String friendsString="";
                for(int i=0;i<friends.length;i++){
                    friendsString+=","+friends[i];
                }   
                friendsString=friendsString.substring(1);
                nameValuePairs.add(new BasicNameValuePair("friends_ids", friendsString));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response=httpclient.execute(httppost);
            } catch (ClientProtocolException e) {
                Log.i(TAG, e.getMessage());
            } catch (IOException e) {
                Log.i(TAG, e.getMessage());
            }
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
        return null;
   }
    @Override
    protected void onPostExecute(Void a) {
        super.onPostExecute(a);
        OSMLoader.osmloader.refreshMap();
    }
}
