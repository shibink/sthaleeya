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
                if (parts.length < 30) {
                    continue;
                }
                try {
                	
                    Merchant merchant = new Merchant(parts[1], parts[2], parts[5], parts[7], 2.5, 
                            Double.parseDouble(parts[28]), Double.parseDouble(parts[29]),parts[30]);
                    ArrayList<MerchantBusinessHours> merchantbusinesshours=new ArrayList<MerchantBusinessHours>();
                    MerchantBusinessHours businessHours=new MerchantBusinessHours("sun",parts[8],parts[9]);
                    merchantbusinesshours.add(businessHours);
                    businessHours=new MerchantBusinessHours("mon",parts[10],parts[11]);
                    merchantbusinesshours.add(businessHours);
                    businessHours=new MerchantBusinessHours("tue",parts[12],parts[13]);
                    merchantbusinesshours.add(businessHours);
                    businessHours=new MerchantBusinessHours("wed",parts[14],parts[15]);
                    merchantbusinesshours.add(businessHours);
                    businessHours=new MerchantBusinessHours("thu",parts[16],parts[17]);
                    merchantbusinesshours.add(businessHours);
                    businessHours=new MerchantBusinessHours("fri",parts[18],parts[19]);
                    merchantbusinesshours.add(businessHours);
                    businessHours=new MerchantBusinessHours("sat",parts[20],parts[21]);
                    merchantbusinesshours.add(businessHours);
                    merchant.setBusinessHours(merchantbusinesshours);
                    merchants.add(merchant);
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IO Exception : ", e);
        }

        return merchants;
    }
}
