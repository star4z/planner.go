package go.planner.plannergo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Handles device
 * Created by bdphi on 2/24/2018.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

    final static String ON_START = "planner.app.startedUp";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("BootBR","intent.getAction()=" + intent.getAction());
        if (ON_START.equals(intent.getAction())) {
            FileIO.readSettings(context);
            FileIO.readAssignmentsFromFile(context);
            NotificationAlarms.setNotificationTimers(context);
        }
    }
}