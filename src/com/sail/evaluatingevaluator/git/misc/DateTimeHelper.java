package com.sail.evaluatingevaluator.git.misc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeHelper {

	public static int stringToMonth(String month) {
		if(month.equals("Jan")) return 1;
		else if(month.equals("Feb")) return 2;
		else if(month.equals("Mar")) return 3;
		else if(month.equals("Apr")) return 4;
		else if(month.equals("May")) return 5;
		else if(month.equals("Jun")) return 6;
		else if(month.equals("Jul")) return 7;
		else if(month.equals("Aug")) return 8;
		else if(month.equals("Sep")) return 9;
		else if(month.equals("Oct")) return 10;
		else if(month.equals("Nov")) return 11;
		else if(month.equals("Dec")) return 12;
		else {
			throw new RuntimeException("Cannot understand month string: "+month);
		}
		
	}
	//Sample Input: Sat Apr 14 08:14:28 2018 +0200
	//Convert the above output to a datetime object
	public static DateTime convert(String input) {
		
		String splits[] = input.split("\\s+");
		int month = DateTimeHelper.stringToMonth(splits[1]);
		int day = Integer.parseInt(splits[2]);
		int year = Integer.parseInt(splits[4]);
		
		String timeSplits[] = splits[3].split(":");
		int hour    = Integer.parseInt(timeSplits[0]);
		int minute  = Integer.parseInt(timeSplits[1]);
		int second  = Integer.parseInt(timeSplits[2]);
		
		DateTime dt = new DateTime(year,month,day,hour, minute, second);
		return dt;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//DateTime dt = DateTimeHelper.convert("Apr 14 08:14:28 2018 +0200");
		DateTimeFormatter dtf =DateTimeFormat.forPattern("EEE MMMM dd HH:mm:ss yyyy Z");
		DateTime dt=dtf.parseDateTime("Sun Oct 16 14:14:22 2011 +0000");
		
			System.out.println(dt);
		
	}

}
