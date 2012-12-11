package com.groupon.sthaleeya;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.os.AsyncTask;
import android.util.Log;

public class AddUserTask extends AsyncTask<Object, Void, Void> {
    private static final String TAG="AddUserTask";

    @Override
    protected Void doInBackground(Object... a) {
        try {
            HttpPost httppost = new HttpPost(
                    Constants.ADD_USER_URL );
            HttpClient httpclient = new DefaultHttpClient();
            
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
                nameValuePairs.add(new BasicNameValuePair("id", a[0].toString()));
                nameValuePairs.add(new BasicNameValuePair("name", a[1].toString()));
                nameValuePairs.add(new BasicNameValuePair("latitude", a[2].toString()));
                nameValuePairs.add(new BasicNameValuePair("longitude", a[3].toString()));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                httpclient.execute(httppost);
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
        return null;
   }
    @Override
    protected void onPostExecute(Void a) {
        super.onPostExecute(a);
    }
}
