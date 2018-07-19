package com.example.intern.todo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;


public class AddTaskViewModel extends ViewModel {

    private LiveData<Task> task;

    public AddTaskViewModel(AppDatabase database, int taskId) {
        task = database.taskDao().loadTaskById(taskId);
    }

    public LiveData<Task> getTask() {
        return task;
    }
}