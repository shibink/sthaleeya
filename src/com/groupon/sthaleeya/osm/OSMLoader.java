package com.groupon.sthaleeya.osm;

import java.util.ArrayList;
import java.util.List;

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
import android.os.Bundle;
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
import com.groupon.sthaleeya.R;
import com.groupon.sthaleeya.dbstore.SQLiteStoreHandler;
import com.groupon.sthaleeya.utils.LocationUtil;

/**
 * Class to load open street map
 */
public class OSMLoader extends Activity implements LocationListener {
    private static final int DIALOG_SHOW_DETAILS = 1;
    private static final String TAG = "OSMLoader";
    private static final double DEF_LATITUDE = 13.0878;
    private static final double DEF_LONGITUDE = 80.2785;
    private static final float ONE_MILE = 1609.34f; // 1 mile = 1609.34 meter
    private static final int REQ_SETTINGS = 0;
    public static enum MERCHANT_STATUS  {CLOSED,ABOUT_TO_CLOSE,OPEN};
    private Category category = Category.ALL;
    private int localRadius = 20; // 20 miles
    private Location defaultLocation;
    private LocationManager locMgr;
    private MapView mapView;
    private MapController mapController;
    private Spinner category_selector;
    private ArrayList<OverlayItem> overlayItemArray = new ArrayList<OverlayItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "OSM loader activity created");
        setContentView(R.layout.activity_osmloader);

        defaultLocation = new Location(LocationManager.GPS_PROVIDER);
        defaultLocation.setLatitude(DEF_LATITUDE);
        defaultLocation.setLongitude(DEF_LONGITUDE);

        // Create Map view and Map controller
        mapView = (MapView) this.findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setClickable(true);
        mapController = mapView.getController();
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(Constants.KEY_RADIUS)) {
            localRadius = extras.getInt(Constants.KEY_RADIUS);
            mapController.setZoom(LocationUtil.getZoomLevel(localRadius));
        } else {
            mapController.setZoom(LocationUtil.DEFAULT_ZOOM);
        }
        if (extras != null && extras.containsKey(Constants.IS_MAP_VIEW)) {
            displayView(extras.getBoolean(Constants.IS_MAP_VIEW));
        }
        locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        addMerchantsToDisplay();
        displayLocation();
        mapView.invalidate();

        ImageView imgView = (ImageView) findViewById(R.id.settings_img);
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Constants.ACTION_SETTINGS);
                intent.putExtra(Constants.KEY_RADIUS, localRadius);
                startActivityForResult(intent, REQ_SETTINGS);
            }
        });

        EditText listView = (EditText) findViewById(R.id.listView);
        listView.setKeyListener(null);
        listView.setFocusable(true);

        category_selector = (Spinner) findViewById(R.id.category);
        category_selector.setOnItemSelectedListener(new OnCategorySelectedListener());

        final Button button1 = (Button) findViewById(R.id.switch_view);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayView(button1.getText().equals(getString(R.string.load_map)));
            }
        });
    }

    private void displayView(boolean isMapView) {
        Button button = (Button) findViewById(R.id.switch_view);
        View mapView = findViewById(R.id.mapview);
        EditText listView = (EditText) findViewById(R.id.listView);
        if (isMapView) {
            listView.setVisibility(View.GONE);
            mapView.setVisibility(View.VISIBLE);
            button.setText(getString(R.string.list_merchants));
        } else {
            // List merchants in text view
            listView.setVisibility(View.VISIBLE);
            mapView.setVisibility(View.GONE);
            button.setText(getString(R.string.load_map));
        }
    }

    private void updateCategory(String categoryStr) {
        Category localCategory = Category.valueOf(Category.class, categoryStr);
        if (localCategory != category) {
            category = localCategory;
            addMerchantsToDisplay();
            displayLocation();
        }
        Log.d(TAG, "Categry changed to " + category);
    }

    class OnCategorySelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            // Set radius based on selected value
            updateCategory(parent.getItemAtPosition(pos).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private void updateRadiusInMap() {
        mapController.setZoom(LocationUtil.getZoomLevel(localRadius));
        addMerchantsToDisplay();
        displayLocation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
        case REQ_SETTINGS:
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (extras != null && extras.containsKey(Constants.KEY_RADIUS)) {
                    int radius = extras.getInt(Constants.KEY_RADIUS);
                    if (radius != localRadius) {
                        localRadius = radius;
                        updateRadiusInMap();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, this);
    }

    @Override
    protected void onStop() {
        locMgr.removeUpdates(this);
        overlayItemArray.clear();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        addMerchantsToDisplay();
        displayLocation(location);
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        Log.d(TAG, "GPS Status changed to " + arg0);
    }

    @Override
    public void onProviderEnabled(String arg0) {
        Log.d(TAG, "GPS Provider Enabled : " + arg0);
    }

    @Override
    public void onProviderDisabled(String arg0) {
        Log.d(TAG, "GPS Provider Disabled : " + arg0);
    }

    private void addMerchantsToDisplay() {
        SQLiteStoreHandler sqlite = new SQLiteStoreHandler();
        List<Merchant> merchants = sqlite.getAllMerchants(category);

        EditText listView = (EditText) findViewById(R.id.listView);
        listView.setText("");
        overlayItemArray.clear();
        Location currentLocation = locMgr
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (currentLocation == null) {
            currentLocation = defaultLocation;
        }

        OverlayItem item = new OverlayItem("You are here !!", "", new GeoPoint(
                currentLocation.getLatitude(), currentLocation.getLongitude()));
        item.setMarker(getResources().getDrawable(R.drawable.balloon_overlay));
        overlayItemArray.add(item);

        Drawable closingMarker = getResources().getDrawable(R.drawable.orange);
        Drawable defaultMarker = getResources().getDrawable(R.drawable.purple);
        int i = 0;
        for (Merchant merchant : merchants) {
            String description = merchant.getAddress() + ", " + merchant.getZip()
                    + "\nPhone: " + merchant.getPhoneNumber();
            item = new OverlayItem(merchant.getName(), description, new GeoPoint(
                    merchant.getLatitude(), merchant.getLongitude()));

            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(merchant.getLatitude());
            location.setLongitude(merchant.getLongitude());
    		
           // if (currentLocation.distanceTo(location) <= (ONE_MILE * localRadius)) {
            	MERCHANT_STATUS check=sqlite.getBusinessHour(merchant);
            	Log.i("dbcheck",check+"");
            	/*if(check==MERCHANT_STATUS.ABOUT_TO_CLOSE)
            		item.setMarker(closingMarker);
            	else if (check ==MERCHANT_STATUS.CLOSED) // Closed
            		continue;*/
                overlayItemArray.add(item);
                listView.append(++i + ". " + merchant.getName() + "\n" + description
                        + "\n\n");
            //}
        }

        if (overlayItemArray.size() <= 1) {
            // Empty merchants. Only current position is there in overlay items
            listView.setText("\n" + getString(R.string.empty_merchants));
        }
        DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(
                this);
        ItemizedIconOverlay<OverlayItem> itemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(
                overlayItemArray, defaultMarker, onItemGestureListener,
                defaultResourceProxyImpl);
        mapView.getOverlays().clear();
        mapView.getOverlays().add(itemizedIconOverlay);
    }

    OnItemGestureListener<OverlayItem> onItemGestureListener = new OnItemGestureListener<OverlayItem>() {

        @Override
        public boolean onItemLongPress(int arg0, OverlayItem arg1) {
            return false;
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean onItemSingleTapUp(int index, OverlayItem item) {
            Bundle extras = new Bundle();
            if (item.mTitle != null) {
                extras.putString(Constants.KEY_NAME, item.mTitle);
            }
            if (item.mDescription != null) {
                extras.putString(Constants.KEY_DETAILS, item.mDescription);
            }
            showDialog(DIALOG_SHOW_DETAILS, extras);
            return true;
        }

    };

    private void displayLocation() {
        displayLocation(locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER));
    }

    private void displayLocation(Location loc) {
        if (loc == null) {
            loc = defaultLocation;
        }

        double latitude = loc.getLatitude();
        double longitude = loc.getLongitude();

        Log.d(TAG, "Location : " + latitude + "," + longitude);
        mapController.setCenter(new GeoPoint(loc));
        mapView.invalidate();
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle extras) {
        Dialog dialog = null;
        AlertDialog.Builder builder = null;

        switch (id) {
        case DIALOG_SHOW_DETAILS:
            if (extras == null || !extras.containsKey(Constants.KEY_DETAILS)) {
                return null;
            }

            builder = new AlertDialog.Builder(this);
            builder.setMessage("");
            if (extras.containsKey(Constants.KEY_NAME)) {
                builder.setTitle(extras.getString(Constants.KEY_NAME));
            } else {
                // we need to set some non empty title due to a bug in android
                builder.setTitle("Current Location");
            }
            builder.setCancelable(false).setNeutralButton(getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            break;
        default:
            dialog = null;
        }

        if (builder != null) {
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                        return true;
                    }
                    return false;
                }
            });
        }

        return dialog;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        if (args.containsKey(Constants.KEY_NAME)) {
            dialog.setTitle(args.getString(Constants.KEY_NAME));
        }
        if (args.containsKey(Constants.KEY_DETAILS)) {
            ((AlertDialog) dialog).setMessage(args.getString(Constants.KEY_DETAILS));
        }
        super.onPrepareDialog(id, dialog, args);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onDestroy() {
        removeDialog(DIALOG_SHOW_DETAILS);
        super.onDestroy();
    }
}