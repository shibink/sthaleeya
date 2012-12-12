package com.groupon.sthaleeya.osm;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import android.widget.TextView;

import com.facebook.FacebookActivity;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.groupon.sthaleeya.AddFriendsTask;
import com.groupon.sthaleeya.AddUserTask;
import com.groupon.sthaleeya.Category;
import com.groupon.sthaleeya.Constants;
import com.groupon.sthaleeya.GetAllMerchantsTask;
import com.groupon.sthaleeya.GetDetailsOfMerchant;
import com.groupon.sthaleeya.R;
import com.groupon.sthaleeya.RetrieveFriendsTask;
import com.groupon.sthaleeya.utils.LocationUtil;

/**
 * Class to load open street map
 */
public class OSMLoader extends FacebookActivity implements LocationListener {
    private final int PICK_FRIENDS_ACTIVITY = 1;
    private static final int DIALOG_SHOW_DETAILS = 1;
    private static final String TAG = "OSMLoader";
    private static final double DEF_LATITUDE = 13.0878;
    private static final double DEF_LONGITUDE = 80.2785;
    private static final float ONE_MILE = 1609.34f; // 1 mile = 1609.34 meter
    private static final int ONE_MINUTE = 60 * 1000;
    private static final int REQ_SETTINGS = 0;
    public static OSMLoader osmloader = null;

    public static enum MERCHANT_STATUS {
        CLOSED, ABOUT_TO_CLOSE, OPEN
    };
    
    
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss Z");
  
    private Category category = Category.ALL;
    private int localRadius = 20; // 20 miles
    private long refreshRate = 30 * ONE_MINUTE; // 30 minutes
    private Location defaultLocation;
    private LocationManager locMgr;
    private MapView mapView;
    private MapController mapController;
    private Handler handler;
    private Spinner category_selector;
    private ArrayList<OverlayItem> overlayItemArray = new ArrayList<OverlayItem>();
    public static final String PREFERENCE_FILE="user_data";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OSMLoader.osmloader = this;
        Log.d(TAG, "OSM loader activity created");
        setContentView(R.layout.activity_osmloader);

        handler = new Handler();
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
        if (extras != null && extras.containsKey(Constants.KEY_REFRESH_RATE)) {
            refreshRate = extras.getInt(Constants.KEY_REFRESH_RATE) * ONE_MINUTE;
        }
        if (extras != null && extras.containsKey(Constants.IS_MAP_VIEW)) {
            displayView(extras.getBoolean(Constants.IS_MAP_VIEW));
        }
        locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
        Button button3 = (Button) findViewById(R.id.authButton);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionState state = OSMLoader.this.getSessionState();
                if (state == null || state.isClosed()) {
                    OSMLoader.this.openSession();
                } else if (state.isOpened()) {
                    OSMLoader.this.closeSessionAndClearTokenInformation();
                }
            }
        });
        ImageView addFriend = (ImageView) findViewById(R.id.add_friends_img);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.groupon.sthaleeya",
                        "com.groupon.sthaleeya.osm.PickFriendsActivity"));
                PickFriendsActivity.populateParameters(intent, null, true, true);
                startActivityForResult(intent, PICK_FRIENDS_ACTIVITY);
            }
        });
        addFriend.setVisibility(View.GONE);
        getUser(this.getSessionState());
    }

    private void getUser(SessionState state) {
        final ImageView addFriend = (ImageView) findViewById(R.id.add_friends_img);
        if (state != null && state.isOpened()) {
            Request request = Request.newMeRequest(this.getSession(),
                    new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            String name = null, id = null;
                            if (user != null) {
                                TextView welcome = (TextView) findViewById(R.id.userName);
                                welcome.setText("Hello " + user.getName() + "!");
                                Location currentLocation = locMgr
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (currentLocation != null) {
                                    Object[] object = new Object[4];
                                    object[0] = user.getId();
                                    object[1] = user.getName();
                                    object[2] = currentLocation.getLatitude();
                                    object[3] = currentLocation.getLongitude();
                                    new AddUserTask().execute(object);
                                }
                                id = user.getId();
                                name = user.getName();
                                addFriend.setVisibility(View.VISIBLE);
                            } else {
                                addFriend.setVisibility(View.GONE);
                            }

                            pushInSharedPref(id, name);
                            refreshMap();
                        }

                    });
            Request.executeBatchAsync(request);
        } else {
            TextView welcome = (TextView) findViewById(R.id.userName);
            welcome.setText("Hello Guest!");
            addFriend.setVisibility(View.GONE);
            pushInSharedPref(null, null);
            refreshMap();
        }
    }

    private void pushInSharedPref(String id, String name) {
        SharedPreferences pref = getSharedPreferences(OSMLoader.PREFERENCE_FILE, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("userId", id);
        editor.putString("userName", name);
        editor.commit();
    }

    @Override
    protected void onSessionStateChange(SessionState state, Exception exception) {
       getUser(state);
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
            refreshMap();
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
        displayLocation(locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER));
    }

    public void refreshMap() {
        refreshMap(locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER));
    }

    public void refreshMap(Location loc) {
        addMerchantsToDisplay();
        displayLocation(loc);
        handler.removeCallbacks(refreshMapRunnable);
        handler.postDelayed(refreshMapRunnable, refreshRate);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case PICK_FRIENDS_ACTIVITY:
            if(resultCode == RESULT_OK){
                Bundle extras = data.getExtras();
                if (extras != null && extras.containsKey("friends_ids")) {
                    String[] friends_ids = extras.getStringArray("friends_ids");
                    SharedPreferences pref=getSharedPreferences(OSMLoader.PREFERENCE_FILE,0);
                    Object[] objectArray=new Object[2];
                    objectArray[0]=pref.getString("userId","0");
                    objectArray[1]=friends_ids;
                    new AddFriendsTask().execute(objectArray);
                }
            }
            break;
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
                if (extras != null && extras.containsKey(Constants.KEY_REFRESH_RATE)) {
                    long rate = extras.getInt(Constants.KEY_REFRESH_RATE) * ONE_MINUTE;
                    if (rate != refreshRate) {
                        refreshRate = rate;
                        handler.removeCallbacks(refreshMapRunnable);
                        handler.postDelayed(refreshMapRunnable, refreshRate);
                    }
                }
                if (extras != null && extras.containsKey("userName")) {
                    String user = extras.getString("userName");
                    TextView welcome = (TextView) findViewById(R.id.userName);
                    welcome.setText("Hello " + user + "!");
                    Log.i(TAG, welcome.getText() + "");
                }
            }
            break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, (ONE_MILE / 2),
                this);
    }

    @Override
    protected void onStop() {
        locMgr.removeUpdates(this);
        overlayItemArray.clear();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Object[] object=new Object[4];
            SharedPreferences pref=getSharedPreferences(OSMLoader.PREFERENCE_FILE,0);
            object[0]=pref.getString("userId","0");
            object[1]=pref.getString("userName","");
            object[2]=location.getLatitude();
            object[3]=location.getLongitude();
            new AddUserTask().execute(object);
        }
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

    // Runnable to refresh map
    private Runnable refreshMapRunnable = new Runnable() {
        @Override
        public void run() {
            // Refresh Map
            refreshMap();
        }
    };

    private String getDescription(Merchant merchant) {
        return merchant.getAddress() + ", " + merchant.getZip() + "\nPhone: "
                + merchant.getPhoneNumber();
    }

    private void addMerchantsToDisplay() {
        SharedPreferences pref=getSharedPreferences(OSMLoader.PREFERENCE_FILE,0);
        new GetAllMerchantsTask().execute(new Object[]{pref.getString("userId", null)});
    }

    public MERCHANT_STATUS getBusinessHour(Merchant merchant) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"
                + merchant.getTimezone()));
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        MerchantBusinessHours businessHours = merchant.getBusinessHours().get(0);

        if (((businessHours.getOpenHr() == hourOfDay) && (businessHours.getOpenMin() <= minutes))
                || ((businessHours.getOpenHr() <= hourOfDay)))
            if (((businessHours.getCloseHr() == hourOfDay) && (businessHours
                    .getCloseMin() >= minutes))
                    || (businessHours.getCloseHr() > hourOfDay)) {

                if (((businessHours.getCloseHr() == (hourOfDay + 1)) && (businessHours
                        .getCloseMin() <= minutes))
                        || (businessHours.getCloseHr() < (hourOfDay) + 1))
                    return OSMLoader.MERCHANT_STATUS.ABOUT_TO_CLOSE;
                else
                    return OSMLoader.MERCHANT_STATUS.OPEN;
            }
        return OSMLoader.MERCHANT_STATUS.CLOSED;
    }
    
    public void loadAll(Object[] objects) {
        List<Merchant> merchants=(List<Merchant>) objects[0];
        List<User> friends=(List<User>) objects[1];
        EditText listView = (EditText) findViewById(R.id.listView);
        listView.setText("");
        overlayItemArray.clear();
        Location currentLocation = locMgr
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (currentLocation == null) {
            currentLocation = defaultLocation;
        }

        String userDescription = getSharedPreferences(OSMLoader.PREFERENCE_FILE, 0)
                .getString("userName", null);
        if (userDescription == null) {
            userDescription = "";
        } else {
            userDescription += "&";
        }
        OverlayItem item = new OverlayItem("user", userDescription + "You are here !!",
                new GeoPoint(currentLocation.getLatitude(),
                        currentLocation.getLongitude()));
        item.setMarker(getResources().getDrawable(R.drawable.balloon_overlay));
        overlayItemArray.add(item);

        Drawable closingMarker = getResources().getDrawable(R.drawable.orange);
        Drawable defaultMarker = getResources().getDrawable(R.drawable.purple);
        Drawable friendsMarker = getResources().getDrawable(R.drawable.user_image);
        int i = 0;
        for (Merchant merchant : merchants) {
            String description = String.valueOf(merchant.getId());
            item = new OverlayItem("merchant", description, new GeoPoint(
                    merchant.getLatitude(), merchant.getLongitude()));

            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(merchant.getLatitude());
            location.setLongitude(merchant.getLongitude());

            if (currentLocation.distanceTo(location) <= (ONE_MILE * localRadius)) {
                MERCHANT_STATUS check = getBusinessHour(merchant);

                if (check == MERCHANT_STATUS.ABOUT_TO_CLOSE) {
                    item.setMarker(closingMarker);
                }
                //else if (check == MERCHANT_STATUS.CLOSED) {
                   // continue;
                //}
                overlayItemArray.add(item);
                listView.append(++i + ". " + merchant.getName() + "\n" + getDescription(merchant)
                        + "\n\n");
            }
        }
        for(User friend:friends){
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(Double.parseDouble(friend.getLatitude()));
            location.setLongitude(Double.parseDouble(friend.getLongitude()));

            if (currentLocation.distanceTo(location) <= (ONE_MILE * localRadius)) {
            	long hour=0, min=0, day=0;
            	Date date = null;
            	try {
					date = dateFormat.parse(friend.getUpdatedTime()+" GMT");
		            Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
					Date current = cal.getTime();
					long diff = current.getTime()-date.getTime();
					min = diff / (1000 * 60);
					hour = min / 60;
					min = min % 60;
					day = hour / 24;
					hour = hour %24;
                } catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}

                StringBuilder diff = new StringBuilder();
                diff.append((day != 0) ? (day + " days, ") : "");
                diff.append((hour != 0) ? (hour + " hours, ") : "");
                diff.append(min + " minutes ago");
                item = new OverlayItem("user", friend.getName() + "&Updated " + diff.toString(),
                        new GeoPoint(Double.parseDouble(friend.getLatitude()),
                                Double.parseDouble(friend.getLongitude())));
                item.setMarker(friendsMarker);
                overlayItemArray.add(item);
            }
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
        mapView.invalidate();
    }

    OnItemGestureListener<OverlayItem> onItemGestureListener = new OnItemGestureListener<OverlayItem>() {

        @Override
        public boolean onItemLongPress(int arg0, OverlayItem arg1) {
            return false;
        }

        @Override
        public boolean onItemSingleTapUp(int index, OverlayItem item) {
            Bundle extras = new Bundle();
            if("user".equals(item.mTitle)){
                String[] parts=item.mDescription.split("&");
                extras.putString(Constants.KEY_NAME, parts[0]);
                if (parts.length > 1) {
                    extras.putString(Constants.KEY_DETAILS, parts[1]);
                }
                showDialog(DIALOG_SHOW_DETAILS, extras);
            }
            else if("merchant".equals(item.mTitle)){
                if (item.mDescription != null && !item.mDescription.isEmpty()) {
                    new GetDetailsOfMerchant().execute(this,
                            Long.parseLong(item.mDescription), extras);
                }
            }
            else if (item.mTitle != null) {
                extras.putString(Constants.KEY_NAME, item.mTitle);
                showDialog(DIALOG_SHOW_DETAILS, extras);
            }
            else {
                extras.putString(Constants.KEY_DETAILS, "");
                showDialog(DIALOG_SHOW_DETAILS, extras);
            }
            return true;
        }
    };

    public void showMerchantOnTap(Merchant merchant, Bundle extras) {
        extras.putString(Constants.KEY_NAME, merchant.getName());
        extras.putString(Constants.KEY_DETAILS, getDescription(merchant));
        showDialog(DIALOG_SHOW_DETAILS, extras);
    }

    private void displayLocation(Location loc) {
        if (loc == null) {
            loc = defaultLocation;
        }

        mapController.setCenter(new GeoPoint(loc));
        mapView.invalidate();
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle extras) {
        Dialog dialog = null;
        AlertDialog.Builder builder = null;

        switch (id) {
        case DIALOG_SHOW_DETAILS:
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
        } else {
            dialog.setTitle("");
        }
        if (args.containsKey(Constants.KEY_DETAILS)) {
            ((AlertDialog) dialog).setMessage(args.getString(Constants.KEY_DETAILS));
        } else {
            ((AlertDialog) dialog).setMessage("");
        }
        super.onPrepareDialog(id, dialog, args);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onDestroy() {
        removeDialog(DIALOG_SHOW_DETAILS);
        super.onDestroy();
    }
}