	package com.groupon.sthaleeya;

	import java.io.IOException;
	import java.io.InputStream;
import java.util.ArrayList;
	import java.util.Arrays;
import java.util.List;
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
	import org.osmdroid.DefaultResourceProxyImpl;
	import org.osmdroid.util.GeoPoint;
	import org.osmdroid.views.MapController;
	import org.osmdroid.views.MapView;
	import org.osmdroid.views.overlay.ItemizedIconOverlay;
	import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
	import org.osmdroid.views.overlay.OverlayItem;

	import android.app.Activity;
	import android.app.AlertDialog;
	import android.app.Dialog;
	import android.content.Context;
	import android.content.DialogInterface;
	import android.content.Intent;
	import android.graphics.drawable.Drawable;
	import android.location.Location;
	import android.location.LocationListener;
	import android.location.LocationManager;
import android.os.AsyncTask;
	import android.os.Bundle;
	import android.os.Handler;
	import android.util.Log;
	import android.view.KeyEvent;
	import android.view.View;
	import android.widget.AdapterView;
	import android.widget.AdapterView.OnItemSelectedListener;
	import android.widget.Button;
	import android.widget.EditText;
	import android.widget.ImageView;
	import android.widget.Spinner;

	import com.groupon.sthaleeya.Category;
	import com.groupon.sthaleeya.Constants;
	import com.groupon.sthaleeya.InsertMerchantTask;
	import com.groupon.sthaleeya.R;
	import com.groupon.sthaleeya.dbstore.JDBCConnection;
	import com.groupon.sthaleeya.utils.LocationUtil;
import com.groupon.sthaleeya.osm.Merchant;
import com.groupon.sthaleeya.osm.MerchantBusinessHours;
import com.groupon.sthaleeya.osm.OSMLoader;


	public class GetAllMerchantsTask extends AsyncTask<Void, Void, List<Merchant>> {
		
			    @Override
	    protected List<Merchant> doInBackground(Void... a) {  
			ArrayList<Merchant> merchants=new ArrayList<Merchant>();
	    	StringBuilder stringBuilder=null;
	    	try {
	            HttpPost httppost = new HttpPost("http://10.1.23.53/sthaleeya_all?category=ALL");
	            HttpClient client = new DefaultHttpClient();
	            HttpResponse response;
	            stringBuilder= new StringBuilder();

	            response = client.execute(httppost);
	            HttpEntity entity = response.getEntity();
	            InputStream stream = entity.getContent();
	            int b;
	            while ((b = stream.read()) != -1) {
	                stringBuilder.append((char) b);
	            }
	            
	          //parse json data
	        	try{
	        	        JSONArray jArray = new JSONArray(stringBuilder.toString());
	        	        for(int i=0;i<jArray.length();i++){
	        	                JSONObject json_data = jArray.getJSONObject(i);
	        	                Merchant newMerchant=new Merchant();
	        	                MerchantBusinessHours businessHours=new MerchantBusinessHours(); 
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
	        	                ArrayList<MerchantBusinessHours> businessArray=new ArrayList<MerchantBusinessHours>();
	        	                businessArray.add(businessHours);
	        	                newMerchant.setBusinessHours(businessArray);
	        	                merchants.add(newMerchant);
	        	        }
	        	}catch(JSONException e){
	        	        Log.e("start", "Error parsing data "+e.toString());
	        	}
	        } catch (Exception e) {
	        	Log.i("start",e.getMessage());
	        } 
	    	
	    	return merchants;
	    }

	    @Override
	    protected void onPostExecute(List<Merchant> merchants) {
	        super.onPostExecute(merchants);
	        OSMLoader.osmloader.loadAllMerchants(merchants);
	    }

}
