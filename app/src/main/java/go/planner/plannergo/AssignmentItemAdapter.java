package go.planner.plannergo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AssignmentItemAdapter extends RecyclerView.Adapter {

    private static String TAG = "AssignmentItemAdapter";

    ArrayList<NewAssignment> dataSet;
    private SharedPreferences prefs;
    private int sortIndex;

    Activity activity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout itemView;
        TextView title, className, date, category;

        ViewHolder(ConstraintLayout itemView) {
            this(itemView, true);
        }

        ViewHolder(ConstraintLayout itemView, boolean swipeable) {
            super(itemView);
            this.itemView = itemView;
            title = this.itemView.findViewById(R.id.title);
            if (swipeable) {
                className = this.itemView.findViewById(R.id.class_name);
                date = this.itemView.findViewById(R.id.date);
                category = this.itemView.findViewById(R.id.category);
            }
        }
    }

    /**
     * Handles clicking of the view (Displays details)
     */
    class BodyClickListener implements View.OnClickListener {
        NewAssignment assignment;

        Activity activity;

        BodyClickListener(NewAssignment assignment, Activity activity) {
            this.assignment = assignment;
            this.activity = activity;
        }

        @Override
        public void onClick(View v) {
            Bundle args = new Bundle();
            args.putLong("uniqueID", assignment.uniqueID);
            args.putInt("sortIndex", sortIndex);

            Intent intent = new Intent(activity, AssignmentDetailsActivity.class);
            intent.putExtras(args);
            activity.startActivityForResult(intent, 1);
        }
    }

    AssignmentItemAdapter(ArrayList<NewAssignment> dataSet, int sortIndex, Activity activity) {
        this.dataSet = dataSet;
        this.sortIndex = sortIndex;
        this.activity = activity;
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_assignment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NewAssignment a = dataSet.get(position);
        ViewHolder vh = (ViewHolder) holder;
        vh.title.setText(a.title);
        vh.category.setText(a.type);
        vh.className.setText(a.className);

        SimpleDateFormat dateFormat = (prefs.getBoolean(SettingsActivity.timeEnabled, false))
                ? new SimpleDateFormat("h:mm a EEE, MM/dd/yy", Locale.US)
                : new SimpleDateFormat("EEE, MM/dd/yy", Locale.US);

        vh.date.setText(dateFormat.format(a.dueDate.getTime()));

        vh.itemView.setOnClickListener(new BodyClickListener(a, activity));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    void removeAt(int position) {
        Log.v("Adapter", "Removed item at position " + position);
        NewAssignment a = dataSet.get(position);
        FileIO.deleteAssignment(activity, a);
        dataSet.remove(position);
        notifyItemRemoved(position);
    }

    void toggleDone(int position) {
        NewAssignment a = dataSet.get(position);
        NewAssignment b = changeAssignmentStatus(a);
        createSnackBarPopup(b);
        dataSet.remove(position);
        notifyItemRemoved(position);
    }

    private NewAssignment changeAssignmentStatus(NewAssignment a) {
        a.completed = !a.completed;
        if (a.completed) {
            FileIO.inProgressAssignments.remove(a);
            FileIO.completedAssignments.add(a);
        } else {
            FileIO.completedAssignments.remove(a);
            FileIO.inProgressAssignments.add(a);
        }
        FileIO.writeFiles(activity);
        notifyDataSetChanged();
        return a;
    }

    /**
     * Creates snackBar in activity with option to undo completion state change.
     * Used only when the assignment is marked done, NOT when it is deleted. (See
     * FileIO.deleteAssignment().)
     *
     * @param a assignment; uses title to give personalized pop-up message.
     */
    private void createSnackBarPopup(final NewAssignment a) {
        String title = (a.title.equals("")) ? "assignment" : "'" + a.title + "'";
        String status = a.completed ? "complete." : "in progress.";

        Log.v(TAG, "title.length=" + title.length());

        if (title.length() > 18){
            title = title.substring(0, 15) + "...'";
        }

        Snackbar snackbar = Snackbar.make(
                activity.findViewById(R.id.coordinator),
                "Marked " + title + " as " + status,
                Snackbar.LENGTH_LONG
        );

        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAssignmentStatus(a);
                dataSet.add(a);
            }
        });

        snackbar.show();
    }


}
