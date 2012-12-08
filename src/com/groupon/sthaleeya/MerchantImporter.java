package com.groupon.sthaleeya;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.groupon.sthaleeya.osm.Merchant;
import com.groupon.sthaleeya.osm.MerchantBusinessHours;


public class MerchantImporter {

    private static final String TAG = "MerchantImporter";

    public static List<Merchant> importMerchants(Context context) {
        List<Merchant> merchants = new ArrayList<Merchant>();

        try {
            InputStream inputStream = context.getAssets().open("merchant-geocode.csv");
            BufferedReader bis = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            bis.readLine(); // Throw away first line
            bis.readLine(); // Throw away 2nd line as well
            while ((line = bis.readLine()) != null) {
                String parts[] = line.split("\t");
               // Log.i("parts length",parts.length+"");
                if (parts.length < 31) {
                    continue;
                }
                try {
                	
                    Merchant merchant = new Merchant(parts[1], parts[2], parts[5], parts[7], 2.5, 
                            Double.parseDouble(parts[28]), Double.parseDouble(parts[29]),parts[30]);
                    ArrayList<MerchantBusinessHours> merchantbusinesshours=new ArrayList<MerchantBusinessHours>();
                    String[] days={"sun","mon","tue","wed","thu","fri","sat"};
                    for(int i=0;i<7;i++){
                    	MerchantBusinessHours businessHours;
                    	parts[8+i]=parts[8+i].trim();
                    	parts[9+i]=parts[9+i].trim();
                    	if(parts[8+i].equals("24 H")||(parts[8+i].equals("in")))
                    		businessHours=new MerchantBusinessHours(days[i],"0","24");
                    	else	
                    		businessHours=new MerchantBusinessHours(days[i],parts[8+i],parts[9+i]);
                    	merchantbusinesshours.add(businessHours);
                    }
                    merchant.setBusinessHours(merchantbusinesshours);
                    merchants.add(merchant);
                } catch (Exception ex) {
                    ex.getMessage();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IO Exception : ", e);
        }

        return merchants;
    }
    
}
