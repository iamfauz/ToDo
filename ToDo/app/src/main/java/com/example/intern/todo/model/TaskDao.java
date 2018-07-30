package com.example.intern.todo.model;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM task")
    LiveData<List<Task>> loadAllTasks();

    @Query("SELECT * FROM task ORDER BY id DESC LIMIT 1")
    List<Task> loadLatestAddedTask();

    @Insert
    void insertTask(Task task);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM task WHERE id = :id")
    LiveData<Task> loadTaskById(int id);

    @Query("SELECT * FROM task WHERE category = :category")
    List<Task> loadTasksByCategory(String category);




}
