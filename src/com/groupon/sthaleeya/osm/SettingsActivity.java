package com.groupon.sthaleeya.osm;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.groupon.sthaleeya.Constants;
import com.groupon.sthaleeya.MerchantImporter;
import com.groupon.sthaleeya.R;
import com.groupon.sthaleeya.dbstore.JDBCConnection;

import com.groupon.sthaleeya.utils.LocationUtil;

public class SettingsActivity extends Activity {
    private static final String OSM_LOADER_CLASS = "com.groupon.sthaleeya.osm.OSMLoader";
    private static final String COMPONENT_NAME = "com.groupon.sthaleeya";

    private Spinner radius_selector;
    private int localRadius = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);

        final EditText edit = (EditText) findViewById(R.id.refresh_rate_value);
        Button button1 = (Button) findViewById(R.id.save_settings);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String refreshRate = edit.getText().toString();
                Intent intent = new Intent();
                intent.putExtra(Constants.KEY_RADIUS, localRadius);
                if (refreshRate != null && !refreshRate.isEmpty()) {
                    intent.putExtra(Constants.KEY_REFRESH_RATE,
                            Integer.parseInt(refreshRate));
                }
                setResult(RESULT_OK, intent);
                SettingsActivity.this.finish();
            }
        });

        Button button2 = (Button) findViewById(R.id.load_map);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayMerchants(true);
            }
        });

        Button button3 = (Button) findViewById(R.id.list_merchants);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayMerchants(false);
            }
        });

        Button uploadMerchants = (Button) findViewById(R.id.upload_merchants);
        uploadMerchants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Merchant> merchants = MerchantImporter
                        .importMerchants(SettingsActivity.this);
                JDBCConnection jdbc=JDBCConnection.getInstance();
                jdbc.populate((ArrayList<Merchant>)merchants);
            }
        });

        LinearLayout refreshLayout = (LinearLayout) findViewById(R.id.refresh_layout);

        String action = getIntent().getAction();
        if (Constants.ACTION_SETTINGS.equals(action)) {
            uploadMerchants.setVisibility(View.GONE);
            button3.setVisibility(View.GONE);
            button2.setVisibility(View.GONE);
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey(Constants.KEY_RADIUS)) {
                localRadius = extras.getInt(Constants.KEY_RADIUS);
            }
        } else {
            refreshLayout.setVisibility(View.GONE);
            button1.setVisibility(View.GONE);
        }

        radius_selector = (Spinner) findViewById(R.id.radius_spinner);
        radius_selector.setSelection(LocationUtil.getIndexOf(localRadius));
        radius_selector.setOnItemSelectedListener(new OnRadiusSelectedListener());
    }

    private void displayMerchants(boolean isMapView) {
        Intent intent = new Intent();
        intent.putExtra(Constants.IS_MAP_VIEW, isMapView);
        intent.putExtra(Constants.KEY_RADIUS, localRadius);
        intent.setComponent(new ComponentName(COMPONENT_NAME, OSM_LOADER_CLASS));
        startActivity(intent);
        finish();
    }

    private void setLocalRadius(String radiusStr) {
        localRadius = Integer.parseInt(radiusStr.substring(0, radiusStr.indexOf(' ')));
    }

    class OnRadiusSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            // Set radius based on selected value
            setLocalRadius(parent.getItemAtPosition(pos).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
}
