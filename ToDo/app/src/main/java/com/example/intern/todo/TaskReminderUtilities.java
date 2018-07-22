package com.example.intern.todo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

public class TaskReminderUtilities {

    private static final int REMINDER_INTERVAL_MINUTES = 1;
    private static final int REMINDER_INTERVAL_SECONDS = (int) (TimeUnit.MINUTES.toSeconds(REMINDER_INTERVAL_MINUTES));
    private static final int SYNC_FLEXTIME_SECONDS = REMINDER_INTERVAL_SECONDS;


    synchronized public static void scheduleChargingReminder(@NonNull final Context context, Task task) {


        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);


        Bundle extrasBundle = new Bundle();
        extrasBundle.putString("task", task.getDescription());
        extrasBundle.putString("date", DateHelper.getDateString(task.getDueDate(),"dd MMM, yyyy, hh:mm a"));
        extrasBundle.putInt("id", task.getId());

        Job TaskReminderJob = dispatcher.newJobBuilder()
                /* The Service that will be used to write to preferences */
                .setService(TaskReminderFirebaseJobService.class)
                /*
                 * Set the UNIQUE tag used to identify this Job.
                 */
                .setTag(task.getId() + "")

                .setLifetime(Lifetime.FOREVER)

                .setRecurring(true)

                .setTrigger(Trigger.executionWindow(
                        REMINDER_INTERVAL_SECONDS,
                        REMINDER_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))

                .setReplaceCurrent(true)
                .setExtras(extrasBundle)
                .build();


        dispatcher.schedule(TaskReminderJob);


    }





}
