package go.planner.plannergo;

import android.os.Bundle;

/**
 * Enabled the extension of the Assignment class without
 *
 * Created by bdphi on 2/28/2018.
 */

public class NewAssignment extends Assignment {
    int priority = 0;
    boolean customNotification;
    long notificationDate = 0L;

    NewAssignment(Assignment assignment, int priority, boolean customNotification, long notificationDate){
        super(assignment.title,
                assignment.className,
                assignment.dueDate,
                assignment.description,
                assignment.completed,
                assignment.type);
        this.priority = priority;
        this.customNotification = customNotification;
        this.notificationDate = notificationDate;
    }

    NewAssignment(Bundle bundle){
        super(bundle);
        dueDate.setTimeInMillis(bundle.getLong("dueDate"));
        priority = bundle.getInt("priority");
        customNotification = bundle.getBoolean("customNotification");
        notificationDate = bundle.getLong("notificationDate");
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
        return new NewAssignment(super.clone(), priority, customNotification, notificationDate);
    }

    @Override
    public String toString() {
        return super.toString() + ",priority=" + priority;
    }

    //TODO: write new assignment types
    static int spinnerPosition(String type){

        return 0;

    }
}
