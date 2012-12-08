package com.groupon.sthaleeya.dbstore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.groupon.sthaleeya.Category;
import com.groupon.sthaleeya.Constants;
import com.groupon.sthaleeya.osm.Merchant;
import com.groupon.sthaleeya.osm.MerchantBusinessHours;
import com.groupon.sthaleeya.osm.OSMLoader;
import com.groupon.sthaleeya.osm.OSMLoader.MERCHANT_STATUS;
import com.groupon.sthaleeya.utils.CursorUtils;

public class SQLiteStoreHandler {
    private static final String TAG = "SQLiteStoreHandler";
    private static final String[] days = { "sun", "mon", "tue", "wed", "thu", "fri",
            "sat" };

    public SQLiteStoreHandler() {
    }

    public long insertMerchants(final List<Merchant> merchants) {
        long rowId = -1;
        SQLiteDatabase db = DbHelper.getInstance().getWritableDatabase();

        if (db == null) {
            return rowId;
        }

        db.beginTransaction();
        try {
            for (Merchant merchant : merchants) {
                ContentValues initialValues = constructMerchantInfo(merchant);
                rowId = db.insertOrThrow(Constants.MERCHANTS_TABLE, null, initialValues);
                if (rowId < 0) {
                    continue;
                }
                List<MerchantBusinessHours> businesshours = merchant.getBusinessHours();
                for (int i = 0; i < businesshours.size(); i++) {
                    ContentValues businessvalues = constructBusinessHours(rowId,
                            businesshours.get(i));
                    db.insertOrThrow(Constants.BUSINESS_TIMINGS_TABLE, null,
                            businessvalues);
                }
            }
        } catch (SQLException exception) {
            Log.e(TAG, "Exception", exception);
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        return rowId;
    }

    /**
     * @param merchant
     */
    private ContentValues constructMerchantInfo(final Merchant merchant) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(Constants.MERCHANT_NAME, merchant.getName());
        initialValues.put(Constants.MERCHANT_ADDRESS, merchant.getAddress());
        initialValues.put(Constants.MERCHANT_ZIP, merchant.getZip());

        initialValues.put(Constants.LATITUDE, merchant.getLatitude());
        initialValues.put(Constants.LONGITUDE, merchant.getLongitude());

        initialValues.put(Constants.MERCHANT_RATING, merchant.getRating());
        initialValues.put(Constants.PHONE_NUM, merchant.getPhoneNumber());
        initialValues.put(Constants.TIMEZONE, merchant.getTimezone());
        return initialValues;
    }

    private ContentValues constructBusinessHours(long id,
            final MerchantBusinessHours businessHours) {
        ContentValues businessValues = new ContentValues();
        businessValues.put(Constants.MERCHANT_ID, id);
        businessValues.put(Constants.BUSINESS_DAY, businessHours.getDay());
        businessValues.put(Constants.BUSINESS_OPEN_HR, businessHours.getOpenHr());
        businessValues.put(Constants.BUSINESS_OPEN_MIN, businessHours.getOpenMin());
        businessValues.put(Constants.BUSINESS_CLOSE_HR, businessHours.getCloseHr());
        businessValues.put(Constants.BUSINESS_CLOSE_MIN, businessHours.getCloseMin());
        return businessValues;
    }

    public Merchant getMerchant(long id) {
        Merchant merchant = null;
        Cursor cursor = null;
        try {
            cursor = getCursor(Constants.MERCHANTS_TABLE, null, "_id=" + id, null, null,
                    null, null);
            if (cursor != null && cursor.moveToFirst()) {
                merchant = Merchant.getMerchantFromCursor(cursor);
            }
        } finally {
            CursorUtils.safeClose(cursor);
        }

        return merchant;
    }

    public List<Merchant> getAllMerchants(Category category) {
        List<Merchant> merchants = new ArrayList<Merchant>();
        Cursor cursor = null;
        String selection = (category == null || category == Category.ALL) ? null
                : "category='" + category.value() + "'";

        try {
            cursor = getCursor(Constants.MERCHANTS_TABLE, null, selection, null, null,
                    null, null);
            if (cursor != null && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    Merchant merchant = Merchant.getMerchantFromCursor(cursor);
                    merchants.add(merchant);
                    cursor.moveToNext();
                }
            }
        } finally {
            CursorUtils.safeClose(cursor);
        }
        return merchants;
    }

    public MERCHANT_STATUS getBusinessHour(Merchant merchant) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"
                + merchant.getTimezone()));
        int day = c.get(Calendar.DAY_OF_WEEK) - 1;

        String day_week = days[day];
        MerchantBusinessHours businessHours = new MerchantBusinessHours();
        String selection = "merchant_id = " + merchant.getId() + " and day='" + day_week
                + "'";

        Cursor cursor = null;
        try {
            cursor = getCursor(Constants.BUSINESS_TIMINGS_TABLE, null, selection, null,
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    businessHours = MerchantBusinessHours.getFromCursor(cursor);
                    break;
                }
            }
        } finally {
            CursorUtils.safeClose(cursor);
        }

        if (((businessHours.getOpenHr() == c.get(Calendar.HOUR_OF_DAY)) && (businessHours
                .getOpenMin() <= (c.get(Calendar.MINUTE))))
                || ((businessHours.getOpenHr() <= c.get(Calendar.HOUR_OF_DAY))))
            if (((businessHours.getCloseHr() == c.get(Calendar.HOUR_OF_DAY)) && (businessHours
                    .getCloseMin() >= (c.get(Calendar.MINUTE))))
                    || (businessHours.getCloseHr() > c.get(Calendar.HOUR_OF_DAY))) {

                if (((businessHours.getCloseHr() == (c.get(Calendar.HOUR_OF_DAY) + 1)) && (businessHours
                        .getCloseMin() <= (c.get(Calendar.MINUTE))))
                        || (businessHours.getCloseHr() < (c.get(Calendar.HOUR_OF_DAY)) + 1))
                    return OSMLoader.MERCHANT_STATUS.ABOUT_TO_CLOSE;
                else
                    return OSMLoader.MERCHANT_STATUS.OPEN;
            }
        return OSMLoader.MERCHANT_STATUS.CLOSED;

    }

    /**
     * 
     * @param table
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @return
     */
    private Cursor getCursor(String table, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having, String orderBy) {
        Cursor cursor = null;
        SQLiteDatabase db = DbHelper.getInstance().getReadableDatabase();
        if (db == null) {
            return null;
        }
        cursor = db.query(table, columns, selection, selectionArgs, groupBy, having,
                orderBy);
        return cursor;
    }
}
