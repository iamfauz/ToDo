package com.example.intern.todo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskAdapterViewHolder> {


    //Data store
    List<Task> mTaskList;

    //Handling Clicks
    public interface ListItemClickListener {

        void onListItemClick(Task task);

    }

    private ListItemClickListener mOnclickListener;


    public TaskAdapter(ListItemClickListener listener) {

        mOnclickListener = listener;

    }


    //ViewHolder Class for normal
    public class TaskAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //Views
        public @BindView(R.id.description)
        TextView descriptionTextView;
        public @BindView(R.id.date)
        TextView dateTextView;

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

        holder.task = mTaskList.get(position);
        holder.descriptionTextView.setText(mTaskList.get(position).getDescription());
        holder.dateTextView.setText(DateHelper.getDateString(mTaskList.get(position).getDueDate(), "dd/MM/yyyy , hh:mm a"));

        }


    @Override
    public int getItemCount() {

        if (mTaskList == null)
            return 0;
        else
            return mTaskList.size();
    }


    public void setTasksData(List<Task> taskList) {
        mTaskList = taskList;
        notifyDataSetChanged();
    }


}
