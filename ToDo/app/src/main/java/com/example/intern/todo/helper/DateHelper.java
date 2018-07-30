package com.example.intern.todo.helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateHelper {



    //Format of date in json file
    public static String dateformat = "dd MMM, yyyy, hh:mm a";

    /* Method that returns the date String formatted as given by dateFormatString
     * @param date i.e the date to be formatted
     * @param dateFormatString i.e the format of the date
     * */
    public static String getDateString(Date date, String dateFormatString){

        DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        String res = dateFormat.format(date);


        return res;
    }


    /* Method that returns the date String formatted as given by dateFormatString
     * @param dateString i.e the date to be formatted
     * @param dateFormatString i.e the format of the date
     * */
    public static String getDateString(String dateString, String dateFormatString){


        Date date = null;

        try {
            SimpleDateFormat format = new SimpleDateFormat(dateformat);
            date = format.parse(dateString);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        String res = dateFormat.format(date);


        return res;




    }



    public static Date getDate(String dateString) {

        Date date = null;

        try {
            SimpleDateFormat format = new SimpleDateFormat(dateformat);
            date = format.parse(dateString);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return date;
    }

    public static Date getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getDate(int hour, int mins) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 0);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, mins);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }


}
