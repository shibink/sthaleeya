package com.groupon.sthaleeya;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.location.Location;
import android.os.AsyncTask;

import com.groupon.sthaleeya.dbstore.SQLiteStoreHandler;
import com.groupon.sthaleeya.osm.Merchant;
import com.groupon.sthaleeya.utils.LocationUtil;

public class InsertMerchantTask extends AsyncTask<Merchant, Void, Merchant[]> {
	public static int getZone(double lat,double lon){
   	 try {
            

            HttpPost httppost = new HttpPost(
                    "http://www.earthtools.org/timezone/"+lat+"/"+lon);
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
            Pattern p=Pattern.compile("<offset>(.*)</offset>");
            Matcher matcher=p.matcher(xml);
            if(matcher.find())
                 return Integer.parseInt(matcher.group(1));
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
   	 return 0;
   }

    @Override
    protected Merchant[] doInBackground(Merchant... merchants) {
        if (merchants == null || merchants.length == 0) {
            return null;
        }
        Location location;
        for (int i = 0; i < merchants.length; i++) {
            String address = merchants[i].getAddress();
            location = LocationUtil.getLocation(address);
            if (location != null) {
                merchants[i].setLatitude(location.getLatitude());
                merchants[i].setLongitude(location.getLongitude());
            }
            int zone=InsertMerchantTask.getZone(location.getLatitude(), location.getLongitude());
            
           // Log.i("zone",zone+"");
            merchants[i].setTimezone(zone);
        }
        return merchants;
    }

    @Override
    protected void onPostExecute(Merchant[] merchants) {
        super.onPostExecute(merchants);

        SQLiteStoreHandler sqlite = new SQLiteStoreHandler();
        sqlite.insertMerchants(Arrays.asList(merchants));
    }
}
