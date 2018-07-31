package com.example.intern.todo.reminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.intern.todo.model.Task;

import java.util.Calendar;
import java.util.TimeZone;

public class AlarmManagerUtilities {

    private static AlarmManager am;

    /** Method that starts the alarm
     *
     * @param context
     * @param task
     */
    public static void startAlarm(Context context, Task task){

        Calendar cal = Calendar.getInstance();
        //cal.setTime(task.getDueDate());
        cal.setTimeZone(TimeZone.getDefault());
        cal.add(Calendar.SECOND, 5);

        am = AlarmManagerSingleton.getInstance(context);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                contentIntent(context, task));

    }

    private static PendingIntent contentIntent(Context context, Task task) {

        Intent startActivityIntent = new Intent(context, AlarmReceiverActivity.class);

        return PendingIntent.getActivity(
                context,
                task.getId(),
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
    /**
     * Method that cancels notification service of a particular task
     */
    public static void cancelAlarm( Context context, Task task) {

        am = AlarmManagerSingleton.getInstance(context);
        Intent startActivityIntent = new Intent(context, AlarmReceiverActivity.class);

        PendingIntent pi = PendingIntent.getActivity(
                context,
                task.getId(),
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        am.cancel(pi);
    }






}
