package com.example.intern.todo.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.intern.todo.model.AppDatabase;
import com.example.intern.todo.model.Task;


public class AddTaskViewModel extends ViewModel {

    private LiveData<Task> task;

    public AddTaskViewModel(AppDatabase database, int taskId) {
        task = database.taskDao().loadTaskById(taskId);
    }

    public LiveData<Task> getTask() {
        return task;
    }
}