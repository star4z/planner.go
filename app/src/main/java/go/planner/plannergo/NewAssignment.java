package go.planner.plannergo;

import android.util.Log;

import java.util.Calendar;

/**
 * Enabled the extension of the Assignment class without
 * <p>
 * Created by bdphi on 2/28/2018.
 */

public class NewAssignment extends Assignment {
    int priority = 0;
    Calendar notificationDate1;
    Calendar notificationDate2;
    long uniqueID; //unique to each assignment; Differentiates assignments that are otherwise identical

    NewAssignment(){
        this(new Assignment());
    }

    NewAssignment(String title, String className, Calendar dueDate, String description,
                  boolean completed, String type, int priority, Calendar notificationDate1,
                  Calendar notificationDate2, long uniqueID)
    {

        super(title,
                className,
                dueDate,
                description,
                completed,
                type);
        this.priority = priority;
        this.notificationDate1 = notificationDate1;
        this.notificationDate2 = notificationDate2;
        this.uniqueID = uniqueID;
    }

    /**
     * NOT a copy constructor!
     * For maintaining files during update
     * @param assignment old Assignment
     */
    NewAssignment(Assignment assignment) {
        this(assignment, 0,  null, null, System.currentTimeMillis());
    }


    NewAssignment(Assignment assignment, int priority, Calendar notificationDate1, Calendar notificationDate2, long uniqueID) {
        super(assignment.title,
                assignment.className,
                assignment.dueDate,
                assignment.description,
                assignment.completed,
                assignment.type);
        this.priority = priority;

        this.notificationDate1 = notificationDate1;

        this.notificationDate2 = notificationDate2;
        this.uniqueID = uniqueID;
    }

    @Override
    protected NewAssignment clone() {
        super.clone();//is a waste, but apparently Java likes this better than not
        return new NewAssignment(title, className, (Calendar) dueDate.clone(), description,
                completed, type, priority, notificationDate1, notificationDate2, uniqueID);
    }

    /**
     * Checks if they are copies of the same original assignment; if they are, they should contain
     * the same information. This might not be true all the time in an AssignmentActivity. To compare
     * fields, use compareFields(NewAssignment).
     * @param o Another object to compare with this one.
     * @return true if they are both instances of NewAssignment and share a unique ID, else false.
     *
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof NewAssignment && ((NewAssignment) o).uniqueID == uniqueID;
    }

    /**
     * Checks if all fields except unique ID between two instances are the same.
     * Normally does not need to be checked.
     * Neglects notificationDates because they are unused.
     * @return true if all editable fields are the same.
     */
    public boolean compareFields(NewAssignment o) {
        Log.v("NewAssignments", String.valueOf(title.equals(o.title)) +
               String.valueOf(className.equals(o.className)) +
                String.valueOf(dueDate.equals(o.dueDate)) +
                String.valueOf(description.equals(o.description)) +
                String.valueOf(completed == o.completed) +
                String.valueOf(type.equals(o.type)) +
                String.valueOf(priority == o.priority));
        return title.equals(o.title)
                && className.equals(o.className)
                && dueDate.equals(o.dueDate)
                && description.equals(o.description)
                && completed == o.completed
                && type.equals(o.type)
                && priority == o.priority;
    }

    @Override
    public int hashCode() {
        return (int) uniqueID;
    }

    /**
     * String representation of NewAssignment
     * @return ID number; ID numbers should only be the same if they reference the same object
     */
    @Override
    public String toString() {
        return "#" + uniqueID + ", " + title;
    }

    public int spinnerPosition(){
        int pos = FileIO.types.indexOf(type);
        Log.v("NewAssignment", "spinnerPosition=" + pos);
        return (pos >= 0) ? pos : 0;
    }
}
