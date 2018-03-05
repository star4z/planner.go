package go.planner.plannergo;

import android.os.Bundle;

import java.util.Calendar;

/**
 * Enabled the extension of the Assignment class without
 * <p>
 * Created by bdphi on 2/28/2018.
 */

public class NewAssignment extends Assignment {
    int priority = 0;
    boolean customNotification;
    long notificationDate = 0L;
    long uniqueID;


    NewAssignment(String title, String className, Calendar dueDate, String description,
                  boolean completed, String type, int priority, boolean customNotification,
                  long notificationDate, long uniqueID)
    {

        super(title,
                className,
                dueDate,
                description,
                completed,
                type);
        this.priority = priority;
        this.customNotification = customNotification;
        this.notificationDate = notificationDate;
        this.uniqueID = uniqueID;
    }

    NewAssignment(Assignment assignment, int priority, boolean customNotification, long notificationDate) {
        this(assignment, priority, customNotification, notificationDate, Calendar.getInstance().getTimeInMillis());
    }

    NewAssignment(Assignment assignment, int priority, boolean customNotification, long notificationDate, long uniqueID) {
        super(assignment.title,
                assignment.className,
                assignment.dueDate,
                assignment.description,
                assignment.completed,
                assignment.type);
        this.priority = priority;
        this.customNotification = customNotification;
        this.notificationDate = notificationDate;
        this.uniqueID = uniqueID;
    }

    NewAssignment(Bundle bundle) {
        super(bundle);
        dueDate.setTimeInMillis(bundle.getLong("dueDate"));
        priority = bundle.getInt("priority");
        customNotification = bundle.getBoolean("customNotification");
        notificationDate = bundle.getLong("notificationDate");
        uniqueID = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    Bundle generateBundle() {
        Bundle bundle = super.generateBundle();
        bundle.putLong("dueDate", dueDate.getTimeInMillis());
        bundle.putInt("priority", priority);
        bundle.putBoolean("customNotification", customNotification);
        bundle.putLong("notificationDate", notificationDate);
        return bundle;
    }

    @Override
    protected NewAssignment clone() {
        return new NewAssignment(super.clone(), priority, customNotification, notificationDate, uniqueID);
    }

    @Override
    public String toString() {
        return super.toString() + ",priority=" + priority;
    }

    //TODO: write new assignment types
    static int spinnerPosition(String type) {

        return 0;

    }
}
