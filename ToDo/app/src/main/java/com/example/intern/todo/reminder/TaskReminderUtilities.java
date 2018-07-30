package com.example.intern.todo.reminder;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.intern.todo.helper.DateHelper;
import com.example.intern.todo.model.Task;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class TaskReminderUtilities {

    private static FirebaseJobDispatcher dispatcher;

    //Notification Spinner Items
    public static ArrayList<String> notificationSpinnerList = new ArrayList<>(Arrays.asList("No Notication",
            "Repeat every hour",
            "Repeat once a day",
            "Repeat once a week",
            "Repeat once a month",
            "Repeat once a year"));

    synchronized public static void scheduleTaskReminder(@NonNull final Context context, Task task) {

        if (!task.getNotificationInterval().equals(notificationSpinnerList.get(0))) {
            //Getting singleton firebase instance
            dispatcher = FirebaseJobDispatcherSingleton.getInstance(context);

            Bundle extrasBundle = new Bundle();
            extrasBundle.putString("task", task.getDescription());
            extrasBundle.putString("date", DateHelper.getDateString(task.getDueDate(), "dd MMM, yyyy, hh:mm a"));
            extrasBundle.putInt("id", task.getId());

            //Getting notification interval in seconds according to task

            int reminderIntervalSeconds = getReminderIntervalSeconds(task);

            Job TaskReminderJob = dispatcher.newJobBuilder()

                    .setService(TaskReminderFirebaseJobService.class)
                    .setTag(task.getId() + "")
                    .setLifetime(Lifetime.FOREVER)
                    .setRecurring(true)
                    .setTrigger(Trigger.executionWindow(
                            reminderIntervalSeconds,
                            reminderIntervalSeconds + 60 ))
                    .setReplaceCurrent(true)
                    .setExtras(extrasBundle)
                    .build();


            dispatcher.schedule(TaskReminderJob);
        }

    }

    /**
     * Method that cancels notification service of a particular task
     */
    public static void deleteReminder(Task task, Context context) {

        dispatcher = FirebaseJobDispatcherSingleton.getInstance(context);
        Log.d("TEST1", task.getId() + "");
        dispatcher.cancel(task.getId() + "");


    }

    public static void deleteAllReminders(Context context) {

        dispatcher = FirebaseJobDispatcherSingleton.getInstance(context);
        dispatcher.cancelAll();

    }

    /**
     * Method that defines notification intervals according to the task
     */
    private static int getReminderIntervalSeconds(Task task) {

        switch (task.getNotificationInterval()) {

            case "Repeat every hour":

                return (int) (TimeUnit.HOURS.toSeconds(1));

            case "Repeat once a day":

                return (int) (TimeUnit.DAYS.toSeconds(1));

            case "Repeat once a week":

                return (int) (TimeUnit.HOURS.toSeconds(168));

            case "Repeat once a month":

                return (int) (TimeUnit.HOURS.toSeconds(730));

            case "Repeat once a year":

                return (int) (TimeUnit.HOURS.toSeconds(8760));


        }

        return 0; //never reach


    }


}
