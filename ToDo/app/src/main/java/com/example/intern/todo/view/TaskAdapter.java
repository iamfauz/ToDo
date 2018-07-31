package com.example.intern.todo.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.intern.todo.R;
import com.example.intern.todo.helper.DateHelper;
import com.example.intern.todo.model.Task;
import com.example.intern.todo.reminder.TaskReminderUtilities;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskAdapterViewHolder> {


    //Data store
    List<Task> mTaskList;
    List<Task> mTaskListCopy;

    Context context;

    int[] colors = { R.color.androidGreen, R.color.steelPink, R.color.sunflower, R.color.colorMartina};

    //Handling Clicks
    public interface ListItemClickListener {

        void onListItemClick(Task task);

    }

    private ListItemClickListener mOnclickListener;


    public TaskAdapter(ListItemClickListener listener , Context context) {

        mOnclickListener = listener;
        this.context = context;

    }


    //ViewHolder Class for normal
    public class TaskAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //Views
        public @BindView(R.id.description)
        TextView descriptionTextView;
        public @BindView(R.id.date)
        TextView dateTextView;
        public @BindView(R.id.overdue)
        TextView overdueTextView;
        @BindView(R.id.category_image_view)
        ImageView categoryImageView;
        @BindView(R.id.bell)
        ImageView bellIcon;

        public Task task;

        public TaskAdapterViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            int position = getAdapterPosition();
            mOnclickListener.onListItemClick(mTaskList.get(position));

        }


    }


    @NonNull
    @Override
    public TaskAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.task_item;

        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new TaskAdapterViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdapterViewHolder holder, int position) {
        Task task = mTaskList.get(position);
        holder.task = task;
        holder.descriptionTextView.setText(task.getDescription());
        holder.dateTextView.setText(DateHelper.getDateString(task.getDueDate(), "dd MMM, yyyy, hh:mm a"));

        //Circular Icon
        TextDrawable drawable = TextDrawable.builder()
                .buildRoundRect( task.getCategory().substring(0,1) ,
                        context.getResources().getColor(colors[AddTaskActivity.categorySpinnerList.indexOf(task.getCategory())]), 70);
        holder.categoryImageView.setImageDrawable(drawable);

        if (task.isOverdueTask())
            holder.overdueTextView.setVisibility(View.VISIBLE);
        else
            holder.overdueTextView.setVisibility(View.GONE);

        if(task.getNotificationInterval().equals(TaskReminderUtilities.notificationSpinnerList.get(0)))
            holder.bellIcon.setImageResource(R.drawable.ic_notifications_off_black_24dp);
        else
            holder.bellIcon.setImageResource(R.drawable.ic_notifications_black_24dp);



    }
        @Override
        public int getItemCount () {

            if (mTaskList == null)
                return 0;
            else
                return mTaskList.size();
        }


        public void setTasksData (List < Task > taskList) {
            mTaskList = taskList;
            mTaskListCopy  = new ArrayList<>();

            if(mTaskList != null && !mTaskList.isEmpty())
                mTaskListCopy.addAll(mTaskList);
                Log.d("Search", "adapter");

            notifyDataSetChanged();
        }


        public List<Task> getTasks () {
            return mTaskList;

        }

    /**
     * Filter for search functionality
     * @param text
     */
    public void filter(String text) {
        if(mTaskListCopy != null && !mTaskListCopy.isEmpty() ) {
            mTaskList.clear();
            if (text.isEmpty()) {
                Log.d("Search", "filter");
                mTaskList.addAll(mTaskListCopy);
            } else {
                Log.d("Search", "filter1");
                text = text.toLowerCase();
                for (Task task : mTaskListCopy) {
                    if (task.getDescription().toLowerCase().contains(text) || DateHelper.getDateString(task.getDueDate(), "dd MMM, yyyy, hh:mm a").toLowerCase().contains(text)) {
                        mTaskList.add(task);
                    }
                }
            }
            notifyDataSetChanged();

        }
    }


    }
