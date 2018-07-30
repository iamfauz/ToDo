package com.example.intern.todo.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.CalendarContract;

import com.example.intern.todo.model.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalenderUtils {

    /* Method that add Session to Calender
     *
     * */
    public static void addSessionToCalender(Context context, String description, String category, Date date) {

        Calendar beginTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();

        try {
            beginTime.setTime(date);
            endTime.setTime(date);
        } catch (Exception e) {

            e.printStackTrace();

        }

        if (Build.VERSION.SDK_INT >= 14) {
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, description)
                    .putExtra(CalendarContract.Events.DESCRIPTION, category)
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

            context.startActivity(intent);
        } else {
            Calendar cal = Calendar.getInstance();
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setType("vnd.android.cursor.item/event");
            intent.putExtra("beginTime", beginTime.getTimeInMillis());
            intent.putExtra("allDay", true);
            intent.putExtra("rrule", "FREQ=YEARLY");
            intent.putExtra("endTime", endTime.getTimeInMillis());
            intent.putExtra("title", description);
            context.startActivity(intent);
        }


    }

}
