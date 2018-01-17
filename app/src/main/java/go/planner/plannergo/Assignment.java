package go.planner.plannergo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Stores info on an oldAssignment for Planner
 * Created by bdphi on 10/23/2017.
 */

public class Assignment implements Comparable, Serializable {
    String title;
    String className;
    Calendar dueDate;
    String description;
    boolean completed;
    String type;

    Assignment() {
        title = "";
        className = "";
        dueDate = Calendar.getInstance();
        description = "";
        completed = false;
        type = "Written";
    }


    Assignment(String title, String className, Calendar dueDate, String description, boolean completed, String type) {
        this.title = title;
        this.className = className;
        this.dueDate = dueDate;
        this.description = description;
        this.completed = completed;
        this.type = type;
    }

    Assignment(String title, String className, Calendar dueDate, String description, boolean completed) {
        this(title, className, dueDate, description, completed, "Written");
    }

    Assignment(String title, String className, Calendar dueDate, String description) {
        this(title, className, dueDate, description, false);
    }

    Assignment(Bundle bundle) {
        title = bundle.getString("title");
        className = bundle.getString("class");
        dueDate = Calendar.getInstance();
        int year = bundle.getInt("year");
        int month = bundle.getInt("month");
        int date = bundle.getInt("date");
        dueDate.set(year, month, date);
        description = bundle.getString("description");
        completed = bundle.getBoolean("completed");
        type = bundle.getString("type");
//        Log.v("Assignment", "bundle constructor, type=" + type);

    }

    Bundle generateBundle() {
        Bundle args = new Bundle();

        args.putString("title", title);
        args.putString("class", className);
        args.putInt("year", dueDate.get(Calendar.YEAR));
        args.putInt("month", dueDate.get(Calendar.MONTH));
        args.putInt("date", dueDate.get(Calendar.DATE));
        args.putString("description", description);
        args.putBoolean("completed", completed);
//        Log.v("Assignment", "generateBundle, type=" + type);
        args.putString("type", type);

        return args;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if (!o.getClass().equals(Assignment.class))
            throw new ClassCastException();
        Assignment other = (Assignment) o;
        return dueDate.compareTo(other.dueDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Assignment)) return false;

        Assignment that = (Assignment) o;

        return title.equals(that.title) && (className != null ? className.equals(that.className) : that.className == null);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + (className != null ? className.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Assignment[title=" + title + ",className=" + className
                + ",description=" + description + ",completed=" + completed + ",type=" + type;
    }

    static int spinnerPosition(String type) {
        switch (type) {
            default:
                Log.v("SpinnerPosition", "default case");
            case "Written":
                return 0;
            case "Studying":
                return 1;
            case "Online":
                return 2;
            case "Project":
                return 3;
        }
    }
}
