package com.example.intern.todo.view;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.CalendarContract;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.intern.todo.R;
import com.example.intern.todo.helper.AppExecutors;
import com.example.intern.todo.helper.CalenderUtils;
import com.example.intern.todo.helper.DateHelper;
import com.example.intern.todo.model.AppDatabase;
import com.example.intern.todo.model.Task;
import com.example.intern.todo.reminder.TaskReminderUtilities;
import com.example.intern.todo.viewmodel.AddTaskViewModel;
import com.example.intern.todo.viewmodel.AddTaskViewModelFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    private int mNotificationId = DEFAULT_TASK_ID;

    private static int id = 0;

    @BindView(R.id.fab)
    FloatingActionButton fabButton;

    @BindView(R.id.task)
    EditText taskEditText;

    @BindView(R.id.spinner_category)
    Spinner categorySpinner;

    @BindView(R.id.spinner_notification)
    Spinner notificationSpinner;

    @BindView(R.id.date)
    EditText dueDateEditText;

    @BindView(R.id.time)
    EditText dueTimeEditText;

    @BindView(R.id.dateButton)
    ImageView dateIcon;

    @BindView(R.id.timeButton)
    ImageView timeIcon;


    //Category Spinner Items
    public static ArrayList<String> categorySpinnerList = new ArrayList<>(Arrays.asList("Default",
            "Personal",
            "Work",
            "Shopping"));

    //Notification Spinner Items
    private ArrayList<String> notificationSpinnerList;

    // Member variable for the Database
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        //Data Binding with ButterKnife
        ButterKnife.bind(this);

        setupFabButton();
        setupSpinners();
        setupDateAndTimePicker();

        //Get Database instance
        mDb = AppDatabase.getInstance(getApplicationContext());

        //Handling configuration changes due to rotation
        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_TASK_ID) ) {
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
     * Method to setup Spinners ( category and notification )
     */
    public void setupSpinners() {


        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, categorySpinnerList);
        categoryAdapter.setDropDownViewResource(R.layout.spin_item);
        categorySpinner.setAdapter(categoryAdapter);

        notificationSpinnerList = TaskReminderUtilities.notificationSpinnerList;
        ArrayAdapter<String> notificationAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, notificationSpinnerList);
        notificationAdapter.setDropDownViewResource(R.layout.spin_item);
        notificationSpinner.setAdapter(notificationAdapter);

    }

    /**
     * Method that retrieves user input and inserts Task to DB
     */
    public void addTaskToDb() {

        String description = taskEditText.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();
        String dueDate = dueDateEditText.getText().toString();
        String dueTime = dueTimeEditText.getText().toString();
        String notificationInterval = notificationSpinner.getSelectedItem().toString();


        if (TextUtils.isEmpty(taskEditText.getText().toString())) {
            taskEditText.setError("Can't leave field empty.");
            return;
        }

        if (TextUtils.isEmpty(dueDateEditText.getText().toString())) {
            dueDateEditText.setError("Can't leave field empty.");
            return;
        }
        if (TextUtils.isEmpty(dueTimeEditText.getText().toString())) {
            dueTimeEditText.setError("Can't leave field empty.");
            return;
        }


        Date date = DateHelper.getDate(dueDate + ", " + dueTime);

        Date today = new Date();
        if(date.before(today)){

            Toast.makeText(this, "Due date has passed.", Toast.LENGTH_SHORT).show();
            dueTimeEditText.setError("Due date passed.");
            dueDateEditText.setError("Due date passed.");
            return;

        }

        final Task task = new Task(description, category, date, notificationInterval);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mTaskId == DEFAULT_TASK_ID) {
                    // insert new task
                    mDb.taskDao().insertTask(task);
                    List<Task> tasks = mDb.taskDao().loadLatestAddedTask();
                    TaskReminderUtilities.scheduleTaskReminder(getApplicationContext(), tasks.get(0));
                    //AlarmManagerUtilities.startAlarm(getApplicationContext(), tasks.get(0));

                } else {
                    //update task
                    task.setId(mTaskId);
                    mDb.taskDao().updateTask(task);
                    Log.d("TESTNEW", String.valueOf(task.getId()));

                    //Cancelling old notification service
                    if (!task.getNotificationInterval().equals(TaskReminderUtilities.notificationSpinnerList.get(0)))
                        TaskReminderUtilities.deleteReminder(task, getApplicationContext());

                    //Cancelling old alarm
                    //AlarmManagerUtilities.cancelAlarm(getApplicationContext(), task);

                    //Starting new notification service
                    TaskReminderUtilities.scheduleTaskReminder(getApplicationContext(), task);
                   // AlarmManagerUtilities.startAlarm(getApplicationContext(), task);
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
        categorySpinner.setSelection(categorySpinnerList.indexOf(task.getCategory()));
        notificationSpinner.setSelection(notificationSpinnerList.indexOf(task.getNotificationInterval()));
        dueDateEditText.setText(DateHelper.getDateString(task.getDueDate(), "dd MMM, yyyy"));
        dueTimeEditText.setText(DateHelper.getDateString(task.getDueDate(), "hh:mm a"));

    }


    /**
     * Method to setup Date and time Dialog with their respective edittexts
     */
    public void setupDateAndTimePicker() {


        dueDateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showDatePickerDialog();


            }
        });
        dateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });


        dueTimeEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showTimePickerDialog();
            }

        });


        timeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

    }

    public void showDatePickerDialog() {

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(AddTaskActivity.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {


                        Date date = DateHelper.getDate(year, monthOfYear, dayOfMonth);


                        dueDateEditText.setText(DateHelper.getDateString(date, "dd MMM, yyyy"));


                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();


    }

    public void showTimePickerDialog() {

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);


        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(AddTaskActivity.this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {

                        Date date = DateHelper.getDate(hourOfDay, minute);
                        dueTimeEditText.setText(DateHelper.getDateString(date, "hh:mm a"));
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();


    }


    /**
     * Hiding keyboard when pressed anywhere else on the screen
     */

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if (v != null &&
                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_TASK_ID, mTaskId);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.menu_add_task, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.addToCalender) {

            String description = taskEditText.getText().toString();
            String category = categorySpinner.getSelectedItem().toString();
            String dueDate = dueDateEditText.getText().toString();
            String dueTime = dueTimeEditText.getText().toString();
            Date date = DateHelper.getDate(dueDate + ", " + dueTime);

            if (TextUtils.isEmpty(taskEditText.getText().toString())) {
                taskEditText.setError("Can't leave field empty.");
                return super.onOptionsItemSelected(item);
            }

            if (TextUtils.isEmpty(dueDateEditText.getText().toString())) {
                dueDateEditText.setError("Can't leave field empty.");
                return super.onOptionsItemSelected(item);
            }
            if (TextUtils.isEmpty(dueTimeEditText.getText().toString())) {
                dueTimeEditText.setError("Can't leave field empty.");
                return super.onOptionsItemSelected(item);
            }

            CalenderUtils.addSessionToCalender(this, description, category, date);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Speech Recognition functionality
     */
    public void getSpeechInput(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());


        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your device doesn't support speech recognition.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    taskEditText.setText(result.get(0));
                }
                break;
        }
    }








}
