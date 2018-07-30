package com.example.intern.todo.reminder;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TaskReminderFirebaseJobService extends com.firebase.jobdispatcher.JobService {

   public static AsyncTask mBackgroundTask;

    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(final com.firebase.jobdispatcher.JobParameters job) {
        //Obtaining Extras
        final String task = job.getExtras().getString("task");
        final String date = job.getExtras().getString("date");
        final int id = job.getExtras().getInt("id");

        mBackgroundTask = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                Context context = TaskReminderFirebaseJobService.this;
                NotificationsUtils.remindUserTask(TaskReminderFirebaseJobService.this, task, date, id);
                return null;
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            protected void onPostExecute(Object o) {

                jobFinished(job, false);
            }
        };

        //Execute the AsyncTask
        mBackgroundTask.execute();

        return true;
    }


    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        if (mBackgroundTask != null) mBackgroundTask.cancel(true);
        return false;
    }
}
