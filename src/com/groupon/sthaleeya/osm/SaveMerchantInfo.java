package com.groupon.sthaleeya.osm;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.groupon.sthaleeya.InsertMerchantTask;
import com.groupon.sthaleeya.R;

public class SaveMerchantInfo extends Activity {
    private Merchant merchant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.insert_merchants);
        Button button1 = (Button) findViewById(R.id.save_details);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMerchantInfo();
            }
        });
    }
    
    private void saveMerchantInfo() {
        merchant = new Merchant();
        EditText editText = (EditText) findViewById(R.id.merchant_name);
        merchant.setName(editText.getText().toString());
        editText = (EditText) findViewById(R.id.address);
        merchant.setAddress(editText.getText().toString());
        editText = (EditText) findViewById(R.id.zip_code);
        merchant.setZip(editText.getText().toString());
        editText = (EditText) findViewById(R.id.phone_number);
        merchant.setPhoneNumber(editText.getText().toString());
        editText = (EditText) findViewById(R.id.ratings);
        merchant.setRating(Double.parseDouble(editText.getText().toString()));
        

        //new InsertMerchantTask().execute(new Merchant[] { merchant });
    }
}
