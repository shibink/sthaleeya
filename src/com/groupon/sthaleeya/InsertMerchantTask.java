package com.groupon.sthaleeya;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class InsertMerchantTask extends AsyncTask<Void, Void, Void> {
    public int getZone(double lat, double lon) {
        try {
            HttpPost httppost = new HttpPost("http://www.earthtools.org/timezone/" + lat
                    + "/" + lon);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();

            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
            String xml = stringBuilder.toString();
            Pattern p = Pattern.compile("<offset>(.*)</offset>");
            Matcher matcher = p.matcher(xml);
            if (matcher.find())
                return Integer.parseInt(matcher.group(1));
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
        return 0;
    }

    @Override
    protected Void doInBackground(Void... merchants) {
        StringBuilder stringBuilder = null;
        try {
            HttpPost httppost = new HttpPost(
                    "http://10.1.23.53/sthaleeya_all?category=ALL");
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
            Log.i("start", stringBuilder.toString());
            // parse json data
            try {
                JSONArray jArray = new JSONArray(stringBuilder.toString());
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    Log.i("start", "id: " + json_data.getInt("_id") + ", name: "
                            + json_data.getString("name"));
                }
            } catch (JSONException e) {
                Log.e("start", "Error parsing data " + e.toString());
            }
        } catch (Exception e) {
            Log.i("start", e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void a) {
        super.onPostExecute(a);

        // SQLiteStoreHandler sqlite = new SQLiteStoreHandler();
        // sqlite.insertMerchants(Arrays.asList(merchants));
    }
}
