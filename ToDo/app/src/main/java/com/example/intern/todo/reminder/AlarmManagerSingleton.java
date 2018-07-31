package com.example.intern.todo.reminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;

public class AlarmManagerSingleton {

    static AlarmManager sInstance;
    private static final Object LOCK = new Object();

    public static AlarmManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {

                sInstance =  (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
            }
        }
        return sInstance;
    }
}
