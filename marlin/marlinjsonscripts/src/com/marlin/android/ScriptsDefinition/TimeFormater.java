package com.marlin.android.ScriptsDefinition;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeFormater {
	private static DateFormat format = new SimpleDateFormat("hh:mm:ss a");

	public static Time StringToTime(String time){
		
		Date date;
		Time x = new Time(0);
		try {
			date = format.parse("18:00:10");
			x= new Time(date.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return x;
	}
	
	public static String TimeToString(Time time){
		if(time == null) {
			 return "";
			 }
		else{
			return format.format(time);
		}
	}

}
