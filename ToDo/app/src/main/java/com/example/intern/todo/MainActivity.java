package com.example.intern.todo;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements TaskAdapter.ListItemClickListener {


    @BindView(R.id.recycler_view_tasks)
    RecyclerView mRecyclerView;

    @BindView(R.id.app_logo)
    ImageView appLogo;

    @BindView(R.id.fab)
    FloatingActionButton fabButton;

    TaskAdapter mTaskAdapter;
    private AppDatabase mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Data Binding with ButterKnife
        ButterKnife.bind(this);

        //Get db instance
        mDb = AppDatabase.getInstance(getApplicationContext());

        //Initialize Views
        initViews();

        //Setup ViewModel
        setupViewModel();

    }

    /**
     * Method to initialize all Views
     */
    public void initViews() {

        initRecyclerView();
        setupFabButton();

    }

    /**
     * Method to initialize recyclerView
     */
    public void initRecyclerView() {

        //RecyclerViewDefinition
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        //Improving performance
        mRecyclerView.setHasFixedSize(true);
        mTaskAdapter = new TaskAdapter(this);
        mRecyclerView.setAdapter(mTaskAdapter);

        //Touch helper to the RecyclerView to recognize when a user swipes to delete an item.
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<Task> tasks = mTaskAdapter.getTasks();
                        Task task = tasks.get(position);

                        if (!task.getNotificationInterval().equals(TaskReminderUtilities.notificationSpinnerList.get(0)))
                            TaskReminderUtilities.deleteReminder(task, getApplicationContext()); //Cancelling Notification service

                        mDb.taskDao().deleteTask(task);
                    }
                });

            }
        }).attachToRecyclerView(mRecyclerView);

    }

    /**
     * Method to setup FAB
     */
    public void setupFabButton() {

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Create a new intent to start an AddTaskActivity
                Intent addTaskIntent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(addTaskIntent);
            }
        });

    }

    /**
     * Method to setup MainViewModel
     */
    private void setupViewModel() {

        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(@Nullable List<Task> taskEntries) {

                Log.d("TEST", "HERERRERE");

                if (taskEntries.size() == 0) {

                    mRecyclerView.setVisibility(View.GONE);
                    appLogo.setVisibility(View.VISIBLE);

                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    appLogo.setVisibility(View.INVISIBLE);
                    mTaskAdapter.setTasksData(taskEntries);
                }
            }
        });

    }

    @Override
    public void onListItemClick(Task task) {

        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
        intent.putExtra(AddTaskActivity.FLAG_UPDATE_ID, task.getId());
        intent.putExtra(AddTaskActivity.NOTIFICATION_UPDATE_ID, task.getId());
        startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.menu_main, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.About) {

            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
