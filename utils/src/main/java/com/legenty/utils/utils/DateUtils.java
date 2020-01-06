package com.legenty.utils.utils;

import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static final String getDate() {


        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(c);


    }

    public static final String getDate(Date c) {


        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(c);


    }
    public static final String getDateTimeSql() {


        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS");
        System.out.println("Current time => " + df.format(c));
        return df.format(c);


    }

    public static final String getDateTimeSql(Date c) {

        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd HH:MM:SS");
        System.out.println("Current time => " + df.format(c));
        return df.format(c);


    }
    public static String getDayName(int day) {
        switch (day) {
            case 0:
                return "Sunday";
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            case 6:
                return "Saturday";
        }

        return "Worng Day";
    }

    public static String getCurrentDay() {
        String weekDay = new String();
        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        if (Calendar.MONDAY == dayOfWeek) weekDay = "Lundi";
        else if (Calendar.TUESDAY == dayOfWeek) weekDay = "Mardi";
        else if (Calendar.WEDNESDAY == dayOfWeek) weekDay = "Mercredi";
        else if (Calendar.THURSDAY == dayOfWeek) weekDay = "Jeudi";
        else if (Calendar.FRIDAY == dayOfWeek) weekDay = "Vendredi";
        else if (Calendar.SATURDAY == dayOfWeek) weekDay = "Samedi";
        else if (Calendar.SUNDAY == dayOfWeek) weekDay = "Dimanche";

        return weekDay;
    }
    public static Date getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }

    public static String dateFormat = "dd-MM-yyyy hh:mm";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);

    public static String convertMilliSecondsToFormattedDate(Long milliSeconds){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return simpleDateFormat.format(calendar.getTime());
    }

}
