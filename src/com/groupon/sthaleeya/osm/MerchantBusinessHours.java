package com.groupon.sthaleeya.osm;

import android.database.Cursor;

import com.groupon.sthaleeya.Constants;
import com.groupon.sthaleeya.utils.CursorUtils;


public class MerchantBusinessHours {
	private String day;
	private int openHr;
	private int openMin;
	private int closeHr;
	private int closeMin;

	public MerchantBusinessHours() {

    }

    public MerchantBusinessHours(String day,String openTime,String closedTime) {
    	this.setDay(day);
    	this.setOpenTime(openTime);
    	this.setCloseTime(closedTime);
    }
    public MerchantBusinessHours(String day,int openHr, int openMin, int closeHr, int closeMin) {
    	this.setDay(day);
    
    }
	public void setDay(String day){
		this.day=day;
	}
	public String getDay(){
		return this.day;
	}
	public void setOpenTime(String time){
		if(time !=""){
			int toAdd=0;
			String parts[]=time.split(" ");
			if(parts.length<2)
				return;
			//Log.i("open",parts[1]);
			if(parts[1].equals("PM"))
				toAdd=12;
			String timeParts[]=parts[0].split(":");
			//Log.i("time",timeParts[0]);
			if(timeParts.length<2)
				return;
			int hr=Integer.parseInt(timeParts[0])+toAdd;
			int min=Integer.parseInt(timeParts[1]);
			this.openHr=hr;
			this.openMin=min;
		}
	}
	public String getOpenTime(){
		return this.openHr+":"+this.openMin;
	}
	public int getOpenHr(){
		return this.openHr;
	}
	public void setOpenHr(int openhr){
		this.openHr=openhr;
	}
	public void setCloseHr(int closehr){
		this.closeHr=closehr;
	}
	public void setOpenMin(int openmin){
		this.openMin=openmin;
	}
	public void setCloseMin(int closemin){
		this.closeMin=closemin;
	}
	public int getOpenMin(){
		return this.openMin;
	}
	public String getCloseTime(){
		return this.closeHr+":"+this.closeMin;
	}
	public int getCloseHr(){
		return this.closeHr;
	}
	public int getCloseMin(){
		return this.closeMin;
	}
	
	public void setCloseTime(String time){
		if(time !=""){
			int toAdd=0;
			String parts[]=time.split(" ");
			if(parts.length<2)
				return;
			if(parts[1].equals("PM"))
				toAdd=12;
			String timeParts[]=parts[0].split(":");
			if(timeParts.length<2)
				return;
			int hr=Integer.parseInt(timeParts[0])+toAdd;
			int min=Integer.parseInt(timeParts[1]);
			this.closeHr=hr;
			this.closeMin=min;
		}
	}
	 public static MerchantBusinessHours getFromCursor(Cursor cursor) {
		   if ((cursor == null) || (cursor.getCount() <= 0)) {
	            return null;
	        }

	        MerchantBusinessHours businessHours = new MerchantBusinessHours();
	        businessHours.openHr = CursorUtils.getIntegerFromCursor(Constants.BUSINESS_OPEN_HR, cursor);
	        businessHours.closeHr = CursorUtils.getIntegerFromCursor(Constants.BUSINESS_CLOSE_HR, cursor);
	        businessHours.closeMin = CursorUtils.getIntegerFromCursor(Constants.BUSINESS_CLOSE_MIN, cursor);
	        businessHours.openMin = CursorUtils.getIntegerFromCursor(Constants.BUSINESS_OPEN_MIN, cursor);
	        return businessHours;
	   }
}
