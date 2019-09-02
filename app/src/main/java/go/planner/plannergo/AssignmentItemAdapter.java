package go.planner.plannergo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class AssignmentItemAdapter extends RecyclerView.Adapter {
    private static String TAG = "AssignmentItemAdapter";

    Activity activity;
    ArrayList<Assignment> dataSet;
    private SharedPreferences prefs;
    private int sortIndex;
    private ColorScheme colorScheme;
    private static HashMap<String, Integer> classColors;


    AssignmentItemAdapter(ArrayList<Assignment> dataSet, int sortIndex, Activity activity) {
        this.dataSet = dataSet;
        this.sortIndex = sortIndex;
        this.activity = activity;
        if (activity instanceof MainActivity)
            colorScheme = ((MainActivity) activity).getColorScheme();
        else {
            Log.d(TAG, "Could not get colorScheme.");
            colorScheme = new ColorScheme(true, activity);
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    /**
     * All-in-one class color code handler
     * Creates new set of colors if necessary
     * Adds a new item to set if necessary
     * Based on class name, returns color int
     *
     * @param className school class name; compared to FileIO.classes data
     * @return color as int from classColors
     */
    private static int getClassColor(String className) {
        if (classColors == null) {
            classColors = new HashMap<>();

            Random random = new Random(System.currentTimeMillis());

            for (String c : FileIO.classNames) {
                int color = generateClassColor(random);
                classColors.put(c, color);
            }
        }

        if (!classColors.containsKey(className)) {
            Random random = new Random(System.currentTimeMillis());

            int color = generateClassColor(random);
            classColors.put(className, color);
        }

        return classColors.get(className);
    }

    /**
     * Creates snackBar in activity with option to undo completion state change.
     * Used only when the assignment is marked done, NOT when it is deleted. (See
     * FileIO.deleteAssignment().)
     *
     * @param a assignment; uses title to give personalized pop-up message.
     */
    private void createSnackBarPopup(final Assignment a) {
        String title = (a.title.equals("")) ? activity.getString(R.string.assignment) : "'" + a.title + "'";
        String status = activity.getString(a.completed ? R.string.eos_c : R.string.eos_ip);

        Log.v(TAG, "title.length=" + title.length());

        if (title.length() > 18) {
            title = title.substring(0, 15) + "...'";
        }

        Snackbar snackbar = Snackbar.make(
                activity.findViewById(R.id.coordinator),
                activity.getString(R.string.marked) + " " + title + " as " + status,
                Snackbar.LENGTH_LONG
        );

        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAssignmentStatus(a);
                dataSet.add(a);
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).loadPanels(
                            a.completed ? FileIO.completedAssignments : FileIO.inProgressAssignments,
                            sortIndex
                    );
                }
            }
        });

        snackbar.show();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_assignment, parent, false);
        return new ViewHolder(v);
    }

    void removeAt(int position) {
        Log.v("Adapter", "Removed item at position " + position);
        Assignment a = dataSet.get(position);
        FileIO.deleteAssignment(activity, a);
        dataSet.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    void toggleDone(int position) {
        Assignment a = dataSet.get(position);
        Assignment b = changeAssignmentStatus(a);
        createSnackBarPopup(b);
        dataSet.remove(position);
        notifyItemRemoved(position);
    }

    private Assignment changeAssignmentStatus(Assignment a) {
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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Assignment a = dataSet.get(position);
        ViewHolder vh = (ViewHolder) holder;

        if (prefs != null && prefs.getBoolean(Settings.classColorsEnabled, false))
            vh.title.setTextColor(getClassColor(a.className));

        vh.title.setText(a.title);
        vh.category.setText(a.type);
        vh.className.setText(a.className);

        SimpleDateFormat dateFormat = (prefs.getBoolean(Settings.timeEnabled, false))
                ? new SimpleDateFormat("h:mm a EEE, MM/dd/yy", Locale.US)
                : new SimpleDateFormat("EEE, MM/dd/yy", Locale.US);

        vh.date.setText(dateFormat.format(a.dueDate.getTime()));

        vh.itemView.setOnClickListener(new BodyClickListener(a, activity));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout itemView;
        TextView title, className, date, category;

        ViewHolder(ConstraintLayout itemView) {
            super(itemView);
            this.itemView = itemView;

            title = this.itemView.findViewById(R.id.title);
            className = this.itemView.findViewById(R.id.class_name);
            date = this.itemView.findViewById(R.id.date);
            category = this.itemView.findViewById(R.id.category);

            int textColor = colorScheme.getColor(ColorScheme.TEXT_COLOR);
            title.setTextColor(textColor);
            className.setTextColor(textColor);
            date.setTextColor(textColor);
            category.setTextColor(textColor);

            this.itemView.setBackgroundColor(colorScheme.getColor(ColorScheme.ASSIGNMENT_VIEW_BG));
        }
    }

    private static int generateClassColor(Random r) {
        return Color.argb(
                255,
                r.nextInt(200),
                r.nextInt(200),
                r.nextInt(200)
        );
    }

    /**
     * Handles clicking of the view (Displays details)
     */
    class BodyClickListener implements View.OnClickListener {
        Assignment assignment;

        Activity activity;

        BodyClickListener(Assignment assignment, Activity activity) {
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


}
