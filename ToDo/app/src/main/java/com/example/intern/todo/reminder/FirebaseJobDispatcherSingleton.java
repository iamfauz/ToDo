package com.example.intern.todo.reminder;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.util.Log;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;

public class FirebaseJobDispatcherSingleton {


    static FirebaseJobDispatcher sInstance;
    private static final Object LOCK = new Object();

    public static FirebaseJobDispatcher getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {

                Driver driver = new GooglePlayDriver(context);
                sInstance= new FirebaseJobDispatcher(driver);
            }
        }

        return sInstance;
    }

}
