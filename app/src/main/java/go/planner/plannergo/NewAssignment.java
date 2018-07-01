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
        this(assignment, 0,  null, null, Calendar.getInstance().getTimeInMillis());
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
        return new NewAssignment(super.clone(), priority, notificationDate1, notificationDate2, uniqueID);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof NewAssignment && ((NewAssignment) o).uniqueID == uniqueID;
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

    //TODO: fix to represent current implementation of type
    public int spinnerPosition(){
        int pos = FileIO.classNames.indexOf(type);
        Log.v("NewAssignment", "spinnerPosition=" + pos);
        return (pos >= 0) ? pos : 0;
    }
}
