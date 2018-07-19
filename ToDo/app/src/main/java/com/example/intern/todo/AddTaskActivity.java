package com.example.intern.todo;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddTaskActivity extends AppCompatActivity {

    // Extra for the task ID to be received in the intent when update is to be done to the task
    public static final String FLAG_UPDATE_ID = "flagUpdateID";

    // Extra for the task ID to be received after rotation
    public static final String INSTANCE_TASK_ID = "instanceTaskId";


    // Constant for default task id to be used when not in update mode
    private static final int DEFAULT_TASK_ID = -1;
    // Constant for logging
    private static final String TAG = AddTaskActivity.class.getSimpleName();

    private int mTaskId = DEFAULT_TASK_ID;

    @BindView(R.id.fab)
    FloatingActionButton fabButton;

    @BindView(R.id.task)
    EditText taskEditText;

    @BindView(R.id.category)
    EditText categoryEditText;

    @BindView(R.id.date)
    EditText dueDateEditText;

    @BindView(R.id.time)
    EditText dueTimeEditText;

    // Member variable for the Database
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        //Data Binding with ButterKnife
        ButterKnife.bind(this);

        setupFabButton();

        //Get Database instance
        mDb = AppDatabase.getInstance(getApplicationContext());

        //Handling configuration changes due to rotation
        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_TASK_ID)) {
            mTaskId = savedInstanceState.getInt(INSTANCE_TASK_ID, DEFAULT_TASK_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(FLAG_UPDATE_ID)) {

            if (mTaskId == DEFAULT_TASK_ID) {
                // populate the UI
                mTaskId = intent.getIntExtra(FLAG_UPDATE_ID, DEFAULT_TASK_ID);
                AddTaskViewModelFactory factory = new AddTaskViewModelFactory(mDb, mTaskId);
                final AddTaskViewModel viewModel
                        = ViewModelProviders.of(this, factory).get(AddTaskViewModel.class);

                // Observe the LiveData object in the ViewModel. Use it also when removing the observer
                viewModel.getTask().observe(this, new Observer<Task>() {
                    @Override
                    public void onChanged(@Nullable Task taskEntry) {
                        viewModel.getTask().removeObserver(this);
                        populateUI(taskEntry);
                    }
                });
            }
        }




    }



    /**
     * Method to setup FAB
     */
    public void setupFabButton() {

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            addTaskToDb();

            }
        });

    }


    /**
     * Method that retrieves user input and inserts Task to DB
     */
    public void addTaskToDb() {

        String description = taskEditText.getText().toString();
        String category = categoryEditText.getText().toString();
        String dueDate = dueDateEditText.getText().toString();
        String dueTime = dueTimeEditText.getText().toString();

        Date date = new Date();


        final Task task = new Task(description, category, date);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mTaskId == DEFAULT_TASK_ID) {
                    // insert new task
                    mDb.taskDao().insertTask(task);
                } else {
                    //update task
                    task.setId(mTaskId);
                    mDb.taskDao().updateTask(task);
                }
                finish();
            }
        });
    }



    /**
     * populateUI would be called to populate the UI when in update mode
     *
     * @param task the taskEntry to populate the UI
     */
    private void populateUI(Task task) {
        if (task == null) {
            return;

        }

        taskEditText.setText(task.getDescription());
        categoryEditText.setText(task.getCategory());
        dueDateEditText.setText(DateHelper.getDateString(task.getDueDate(), "dd MMM, yyyy"));
        dueTimeEditText.setText(DateHelper.getDateString(task.getDueDate(), "hh:mm a"));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_TASK_ID, mTaskId);
        super.onSaveInstanceState(outState);
    }


}
