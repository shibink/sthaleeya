	package com.groupon.sthaleeya;

	import java.io.InputStream;
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


	public class GetAllMerchantsTask extends AsyncTask<Void, Void, List<Merchant>> {
		
			    @Override
	    protected List<Merchant> doInBackground(Void... a) {  
			ArrayList<Merchant> merchants=new ArrayList<Merchant>();
	    	StringBuilder stringBuilder=null;
	    	try {
	            HttpPost httppost = new HttpPost("http://10.1.23.243/sthaleeya_all?category=ALL");
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
