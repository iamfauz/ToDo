package com.example.intern.todo.view;

import android.app.Activity;
import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.intern.todo.R;
import com.example.intern.todo.helper.AppExecutors;
import com.example.intern.todo.model.AppDatabase;
import com.example.intern.todo.model.Task;
import com.example.intern.todo.reminder.TaskReminderUtilities;
import com.example.intern.todo.viewmodel.MainViewModel;
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

        //Seeting up app logo on toolbar
        setupToolbar();

        //Get db instance
        mDb = AppDatabase.getInstance(getApplicationContext());

        //Initialize Views
        initViews();

        //Setup ViewModel
        setupViewModel();

        handleIntent(getIntent());
    }

    /**
     * Method to initialize all Views
     */
    public void initViews() {

        initRecyclerView();
        setupFabButton();

    }

    /**
     * Method to setup Toolbar
     */
    public void setupToolbar(){

        //Seeting up app logo on toolbar
        getSupportActionBar().setTitle(" ToDo");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_done_all_black_24dp);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
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
        mTaskAdapter = new TaskAdapter(this, this);
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

                        //Cancelling Notification service
                        if (!task.getNotificationInterval().equals(TaskReminderUtilities.notificationSpinnerList.get(0)))
                            TaskReminderUtilities.deleteReminder(task, getApplicationContext());
                        //Cancelling alarm
                       // AlarmManagerUtilities.cancelAlarm(getApplicationContext(), task);

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

                if (taskEntries.size() == 0) {
                    Log.d("Search", "main");
                    mTaskAdapter.setTasksData(taskEntries);
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

        startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem mSearchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) mSearchMenuItem.getActionView();


        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mTaskAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mTaskAdapter.filter(newText);
                return true;
            }
        } );


        mSearchMenuItem.setOnActionExpandListener( new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mTaskAdapter.filter("");
                return true;
            }
        });

        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }


    @Override
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
    }


    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("Voice", query);
            mTaskAdapter.filter(query);
        }
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



}
