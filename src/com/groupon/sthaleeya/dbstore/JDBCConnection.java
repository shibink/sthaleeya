package com.groupon.sthaleeya.dbstore;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import com.groupon.sthaleeya.Category;
import com.groupon.sthaleeya.Constants;
import com.groupon.sthaleeya.osm.Merchant;
import com.groupon.sthaleeya.osm.MerchantBusinessHours;
import com.groupon.sthaleeya.osm.OSMLoader;
import com.groupon.sthaleeya.osm.OSMLoader.MERCHANT_STATUS;
import com.groupon.sthaleeya.utils.CursorUtils;

import android.database.Cursor;
import android.util.Log;

public class JDBCConnection {
	private Connection connection=null;
	public static JDBCConnection jdbc=null;
	private static final String[] days = { "sun", "mon", "tue", "wed", "thu", "fri",
    "sat" };
	private JDBCConnection(){
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}catch(Exception e){
			Log.e("DB-ERROR","connection to db failed:"+e.getMessage());
		}
		try{
			String url="jdbc:mysql://10.1.23.53/"+Constants.DATABASE_NAME+"?user=sthaleeya&password=password";
			connection = DriverManager.getConnection(url);
		}catch(SQLException e){
			Log.e("DB-ERROR","connection initialization failed:"+e.getMessage());
		}
		createMerchantDB();
		createBusinessDB();
	}
	public static JDBCConnection getInstance(){
		if(jdbc==null)
			jdbc=new JDBCConnection();
		return jdbc;
	}
	public void dropMerchantDB(){
		String query="drop table merchants_table";
		try{
			Statement st=connection.createStatement();
			st.execute(query);
		}catch(Exception e){
			Log.i("JDBC CONNECTION",e.getMessage());
		}
	}
	public void createBusinessIndex(){
		String query="create index merchant_business_hours_index on business_timings (merchant_id)";
		try{
			Statement st=connection.createStatement();
			st.execute(query);
		}catch(Exception e){
			Log.i("JDBC CONNECTION",e.getMessage());
		}
	}

	public void dropBusinessDB(){
		String query="drop  table business_timings";
		try{
			Statement st=connection.createStatement();
			st.execute(query);
		}catch(Exception e){
			Log.i("JDBC CONNECTION",e.getMessage());
		}
	}
	public void createMerchantDB (){
		String query="create table if not exists "
            + "merchants_table"
            + " (_id integer AUTO_INCREMENT, name text not null, address text not null, zip_code text,"
            + "phone_no text, rating double default 0, timezone varchar(10) default null,"
            + "latitude double default 0, longitude double default 0, category varchar(10) default 'ALL',"
            + "PRIMARY KEY(_id),UNIQUE KEY(name(50),latitude,longitude));";
		try{
			Statement st=connection.createStatement();
			st.execute(query); 
		}catch(Exception e){
			Log.i("JDBC CONNECTION",e.getMessage());
		}
		
	}
	public void createBusinessDB(){
		String query="create table if not exists "
            + "business_timings"
            + "(_id integer AUTO_INCREMENT,day varchar(10),openHr int default 0,"
            + "openMin int default 0,closeHr int default 0,closeMin int default 0, merchant_id int,"
            + "FOREIGN KEY (merchant_id) REFERENCES merchants_table" 
            + "(_id)" + ",PRIMARY KEY(_id))";

		try{
			Statement st=connection.createStatement();
			st.execute(query);
		}catch(Exception e){
			Log.i("JDBC CONNECTION",e.getMessage());
		}
	}
	public int  insertMerchant(String name,String address,String zip_code,String phone_no,double rating,String timezone,double latitude,
								double longitude,String category){
		name=name.replace("'","\\'");
		address=address.replace("'","\\'");
		String query="insert into merchants_table (name,address,zip_code,phone_no,rating,timezone,latitude,longitude,category) values ('"+name+"','"+address+"','"+zip_code+"','"+phone_no+"',"+rating+",'"+timezone+"',"+latitude+","+longitude+",'"+category+"') on duplicate key update address='"+address+"',zip_code='"+zip_code+"',phone_no='"+phone_no+"',rating="+rating+",timezone='"+timezone+"',category='"+category+"'";
		int no=0;

		try{
			Statement st=connection.createStatement();
			no=st.executeUpdate(query,Statement.RETURN_GENERATED_KEYS);
		}catch(Exception e){
			Log.i("JDBC CONNECTION",e.getMessage());
		}
		return no;
	}
	public void insertBusinessHours(String day,int openHr,int openMin,int closeHr,int closeMin,int id){
		String query="insert into business_timings(day,openHr,openMin,closeHr,closeMin,merchant_id) values('"+day+"',"+openHr+","+openMin+","+closeHr+","+closeMin+","+id+")";
			try{
				Statement st=connection.createStatement();
				st.execute(query);
			}catch(Exception e){
				Log.i("JDBC CONNECTION",e.getMessage());
			}

		}
	public void dropIndex(){
		String query="drop index merchant_business_hours_index on business_timings";
		try{
			Statement st=connection.createStatement();
			st.execute(query);
		}catch(Exception e){
			Log.i("JDBC CONNECTION",e.getMessage());
		}
	}
	public void populate(ArrayList<Merchant> merchants){
		for(Merchant merchant:merchants)
		{
           int id=insertMerchant(merchant.getName(),merchant.getAddress(),merchant.getZip(),merchant.getPhoneNumber(),merchant.getRating(),merchant.getTimezone(),merchant.getLatitude(),merchant.getLongitude(),"ALL");
           for(MerchantBusinessHours businessHours:merchant.getBusinessHours()){
        	   insertBusinessHours(businessHours.getDay(),businessHours.getOpenHr(),businessHours.getOpenMin(),businessHours.getCloseHr(),businessHours.getCloseMin(),id);
           }
		}
        
	}
	//get all merchants by category
	//get business hours by merchant-id
	//add a user
	//invite friends
	//update location of user
	//get all friends of a user
	public Merchant getMerchant(long id){
		String query="select * from merchants_timing where _id="+id;
		Statement statement=null;
		ResultSet resultset=null;
		Merchant merchant=null;
		try{
			statement=connection.createStatement();
			resultset=statement.executeQuery(query);
			while(resultset.next()){
				merchant=new Merchant(resultset.getString(Constants.MERCHANT_NAME),resultset.getString(Constants.MERCHANT_ADDRESS),
						resultset.getString(Constants.MERCHANT_ZIP),resultset.getString(Constants.PHONE_NUM),
						resultset.getDouble(Constants.MERCHANT_RATING),resultset.getDouble(Constants.LATITUDE),
						resultset.getDouble(Constants.LONGITUDE),resultset.getString(Constants.TIMEZONE));
				merchant.setId(id);
			}
		}catch(Exception e){
			Log.e("DB-ERROR","Error in getting the merchant list:"+e.getMessage());
		}
		return merchant;
	}
	public ArrayList<Merchant> getAllMerchants (Category category){
		ArrayList<Merchant> returnList=new ArrayList<Merchant>();
		String getMerchantsQuery="Select * from "+Constants.MERCHANTS_TABLE
				+" where "+Constants.CATEGORY+" ='"+category.toString()+"'";
		Statement statement=null;
		ResultSet resultset=null;
		try{
			statement=connection.createStatement();
			resultset=statement.executeQuery(getMerchantsQuery);
			while(resultset.next()){
				Merchant merchant=new Merchant(resultset.getString(Constants.MERCHANT_NAME),resultset.getString(Constants.MERCHANT_ADDRESS),
						resultset.getString(Constants.MERCHANT_ZIP),resultset.getString(Constants.PHONE_NUM),
						resultset.getDouble(Constants.MERCHANT_RATING),resultset.getDouble(Constants.LATITUDE),
						resultset.getDouble(Constants.LONGITUDE),resultset.getString(Constants.TIMEZONE));
				merchant.setId(resultset.getInt(Constants._ID));
				returnList.add(merchant);
			}
		}catch(Exception e){
			Log.e("DB-ERROR","Error in getting the merchant list:"+e.getMessage());
		}
		return returnList;
	}
	public MerchantBusinessHours getBusinessHoursForADay(Merchant merchant,String day){
		String getMerchantsQuery="Select * from "+Constants.BUSINESS_TIMINGS_TABLE
				+" where "+Constants.BUSINESS_MERCHANT_ID+"="+merchant.getId()+" and "+Constants.BUSINESS_DAY+" ='"+day+"'";
		Statement statement=null;
		ResultSet resultset=null;
		MerchantBusinessHours businesshours=null;
		try{
			statement=connection.createStatement();
			resultset=statement.executeQuery(getMerchantsQuery);
			while(resultset.next()){
				businesshours=new MerchantBusinessHours(day,resultset.getInt(Constants.BUSINESS_OPEN_HR),
						resultset.getInt(Constants.BUSINESS_OPEN_MIN),resultset.getInt(Constants.BUSINESS_CLOSE_HR),
						resultset.getInt(Constants.BUSINESS_CLOSE_MIN));
				break;
			}
		}catch(Exception e){
			Log.e("DB-ERROR","Error in getting the merchant business hours list:"+e.getMessage());
		}
		return businesshours;
	}

	 public MERCHANT_STATUS getBusinessHour(Merchant merchant) {
	        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"
	                + merchant.getTimezone()));
	        int day = c.get(Calendar.DAY_OF_WEEK) - 1;

	        String day_week = days[day];
	        MerchantBusinessHours businessHours = getBusinessHoursForADay(merchant,day_week);
	        

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

}
