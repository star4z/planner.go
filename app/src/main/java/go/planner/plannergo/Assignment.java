package go.planner.plannergo;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Stores info on an assignment for Planner
 * Created by bdphi on 10/23/2017.
 */

public class Assignment implements Comparable, Serializable{
    String title;
    String className;
    Calendar dueDate;
    String description;
    boolean completed;
    HomeworkType type;


    public Assignment(String title, String className, Calendar dueDate, String description, boolean completed, HomeworkType type) {
        this.title = title;
        this.className = className;
        this.dueDate = dueDate;
        this.description = description;
        this.completed = completed;
        this.type = type;
    }

    public Assignment(String title, String className, Calendar dueDate, String description, boolean completed){
        this(title, className, dueDate, description, completed, HomeworkType.WRITTEN);
    }

    public Assignment (String title, String className, Calendar dueDate, String description){
        this(title, className, dueDate, description, false, HomeworkType.WRITTEN);
    }

    public Assignment(String unparsedInfoString){
        String[] infoPieces = unparsedInfoString.split("z");
        for (String next: infoPieces)
            Log.v("Assignment", next);

        title = infoPieces[0].trim();
        className = infoPieces[1].trim().toUpperCase();

        dueDate = Calendar.getInstance();
        description = infoPieces[3];
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

        if (!title.equals(that.title)) return false;
        return className != null ? className.equals(that.className) : that.className == null;
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + (className != null ? className.hashCode() : 0);
        return result;
    }

    private enum HomeworkType{
        ONLINE, WRITTEN, PROJECT, STUDYING
    }
}
