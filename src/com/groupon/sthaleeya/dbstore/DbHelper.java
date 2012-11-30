package com.groupon.sthaleeya.dbstore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.groupon.sthaleeya.Constants;
import com.groupon.sthaleeya.OsmApplication;

/**
 * This class provides a wrapper for the underlying SQLite DB used to store the
 * data for open street map
 *
 * To upgrade the database schema: 1. Increment DATABASE_VERSION
 *
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DbHelper";
    private static DbHelper instance = null;

    /**
     * Database creation sql statements
     */
    public static final String CREATE_OSM_TABLE = "create table "
            + Constants.MERCHANTS_TABLE
            + " (_id integer primary key autoincrement, name text not null, address text not null, zip_code text,"
            + "phone_no text, rating double default 0, timezone varchar(10) default null,"
            + "latitude double default 0, longitude double default 0, category text default 'ALL');";

public static final String CREATE_BUSINESS_TIMINGS_TABLE="create table "
    		+ Constants.BUSINESS_TIMINGS_TABLE
    		+"(_id integer primary key autoincrement,day varchar(10),openHr int default 0," 
    		+"openMin int default 0,closeHr int default 0,closeMin int default 0 ,merchant_id int," 
    		+"FOREIGN KEY (merchant_id) REFERENCES "+Constants.MERCHANTS_TABLE+"(_id)"
    		+ ")";
    public static final String DROP_MERCHANT_TABLE = "DROP TABLE IF EXISTS "
        + Constants.MERCHANTS_TABLE;
    public static final String DROP_BUSINESS_TIMINGS_TABLE="DROP TABLE IF EXISTS "
    	+ Constants.BUSINESS_TIMINGS_TABLE	;	

    private DbHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.OSM_DATABASE_VERSION);
    }

    public static DbHelper getInstance() {
        if (instance == null) {
            instance = new DbHelper(OsmApplication.getAppContext());
        }
        return instance;
    }
    
    public static DbHelper getInstance(Context context){
        if (instance == null) {
            instance = new DbHelper(context);
        }
        return instance;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        Log.i(TAG, "on Open Database");

        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "on Create Database");

        createTables(db);
        // TODO: Create Index for faster DB queries
    }

	/**
	 * @param db
	 */
	public void createTables(SQLiteDatabase db) {
		db.execSQL(CREATE_OSM_TABLE);
		db.execSQL(CREATE_BUSINESS_TIMINGS_TABLE);
	}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            removeTables(db);
            onCreate(db);
            return;
    }

	/**
	 * drops all tables
	 * @param db
	 */
	public void removeTables(SQLiteDatabase db) {
        db.execSQL(DROP_MERCHANT_TABLE);
        db.execSQL(DROP_BUSINESS_TIMINGS_TABLE);
	}
}
