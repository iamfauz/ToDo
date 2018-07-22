package com.example.intern.todo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "task")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String description;
    private String category;

    @ColumnInfo(name = "due_date")
    private Date dueDate ;


    @Ignore //So that room uses other contructor
    public Task(String description,String category, Date dueDate) {

        this.description = description;
        this.category = category;
        this.dueDate = dueDate;
    }

    public Task(int id, String description, String category, Date dueDate) {

        this.id = id;
        this.description = description;
        this.category = category;
        this.dueDate = dueDate;

    }


    //Getters And Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
}
