package com.example.intern.todo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

    /* Method that returns the date String formatted as given by dateFormatString
     * @param date i.e the date to be formatted
     * @param dateFormatString i.e the format of the date
     * */
    public static String getDateString(Date date, String dateFormatString){

        DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        String res = dateFormat.format(date);


        return res;
    }



}
