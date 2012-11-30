package com.groupon.sthaleeya.dbstore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.groupon.sthaleeya.Constants;
import com.groupon.sthaleeya.osm.Merchant;
import com.groupon.sthaleeya.osm.MerchantBusinessHours;

import android.util.Log;

public class JDBCConnection {
	public static Connection connection=null;
	public JDBCConnection(){
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}catch(Exception e){
			Log.e("DB-ERROR","connection to db failed:"+e.getMessage());
		}
		try{
			String url="jdbc:mysql://localhost/"+Constants.DATABASE_NAME+"?user=root&password=";
			connection = DriverManager.getConnection(url);
		}catch(SQLException e){
			Log.e("DB-ERROR","connection initialization failed:"+e.getMessage());
		}
	}
	//get all merchants by category
	//get business hours by merchant-id
	//add a user
	//invite friends
	//update location of user
	//get all friends of a user
	public ArrayList<Merchant> getAllMerchants (String category){
		ArrayList<Merchant> returnList=new ArrayList<Merchant>();
		String getMerchantsQuery="Select * from "+Constants.MERCHANTS_TABLE
				+" where "+Constants.CATEGORY+" ='"+category+"'";
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
	public Merchant getBusinessHoursForMerchant(Merchant merchant,String day){
		String getMerchantsQuery="Select * from "+Constants.BUSINESS_TIMINGS_TABLE
				+" where "+Constants.BUSINESS_MERCHANT_ID+"="+merchant.getId()+" and "+Constants.BUSINESS_DAY+" ='"+day+"'";
		Statement statement=null;
		ResultSet resultset=null;
		try{
			statement=connection.createStatement();
			resultset=statement.executeQuery(getMerchantsQuery);
			while(resultset.next()){
				ArrayList<MerchantBusinessHours> businesshours_array=new ArrayList<MerchantBusinessHours>(); 
				MerchantBusinessHours businesshours=new MerchantBusinessHours(day,resultset.getInt(Constants.BUSINESS_OPEN_HR),
						resultset.getInt(Constants.BUSINESS_OPEN_MIN),resultset.getInt(Constants.BUSINESS_CLOSE_HR),
						resultset.getInt(Constants.BUSINESS_CLOSE_MIN));
				businesshours_array.add(businesshours);
				merchant.setBusinessHours(businesshours_array);
				break;
			}
		}catch(Exception e){
			Log.e("DB-ERROR","Error in getting the merchant business hours list:"+e.getMessage());
		}
		return merchant;
	}
}
