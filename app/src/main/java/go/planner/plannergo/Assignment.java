package go.planner.plannergo;

import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Enabled the extension of the Assignment class without
 * <p>
 * Created by bdphi on 2/28/2018.
 */

public class Assignment implements Serializable {
    private final static String TAG = "Assignment";
    String title;
    String className;
    Calendar dueDate;
    String description;
    boolean completed;
    String type;
    int priority = 0;
    Calendar notificationDate1;
    Calendar notificationDate2;
    long uniqueID; //unique to each assignment; Differentiates assignments that are otherwise identical

    Assignment() {
        title = "";
        className = "";
        dueDate = Calendar.getInstance();
        description = "";
        completed = false;
        type = "Written";
        uniqueID = System.currentTimeMillis();
    }

    Assignment(String title, String className, Calendar dueDate, String description,
               boolean completed, String type, int priority, Calendar notificationDate1,
               Calendar notificationDate2, long uniqueID)
    {
        this.title = title;
        this.className = className;
        this.dueDate = dueDate;
        this.description = description;
        this.completed = completed;
        this.type = type;
        this.priority = priority;
        this.notificationDate1 = notificationDate1;
        this.notificationDate2 = notificationDate2;
        this.uniqueID = uniqueID;
    }

    Assignment(Assignment assignment) {
        title = assignment.title;
        className = assignment.className;
        dueDate = assignment.dueDate;
        description = assignment.description;
        completed = assignment.completed;
        type = assignment.type;
        priority = assignment.priority;
        notificationDate1 = assignment.notificationDate1;
        notificationDate2 = assignment.notificationDate2;
        uniqueID = assignment.uniqueID;
    }

    /**
     * Checks if they are copies of the same original assignment; if they are, they should contain
     * the same information. This might not be true all the time in an AssignmentActivity. To compare
     * fields, use compareFields(Assignment).
     * @param o Another object to compare with this one.
     * @return true if they are both instances of Assignment and share a unique ID, else false.
     *
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Assignment && ((Assignment) o).uniqueID == uniqueID;
    }

    @Override
    public int hashCode() {
        return (int) uniqueID;
    }

    /**
     * String representation of Assignment
     *
     * @return ID number; ID numbers should only be the same if they reference the same object
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return "#" + uniqueID + ", " + title;
    }


    public int spinnerPosition() {
        int pos = FileIO.types.indexOf(type);
        Log.v(TAG, "spinnerPosition=" + pos);
        return Math.max(pos, 0);
    }

    /**
     * Checks if all fields except unique ID between two instances are the same.
     * Normally does not need to be checked.
     * Neglects notificationDates because they are unused.
     * @return true if all editable fields are the same.
     */
    public boolean compareFields(Assignment o) {
        return title.equals(o.title)
                && className.equals(o.className)
                && dueDate.equals(o.dueDate)
                && description.equals(o.description)
                && completed == o.completed
                && type.equals(o.type)
                && priority == o.priority;
    }
}
