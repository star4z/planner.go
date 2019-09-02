package go.planner.plannergo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;

/**
 * Handles notifications with
 * Created by bdphi on 2/25/2018.
 */

class NotificationAlarms {
    private static final String TAG = "NotificationsAlarms";

    static void setNotificationTimers(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notifEnabled = prefs.getBoolean(Settings.notifEnabled, true);
        Log.d(TAG, "notifEnabled = " + notifEnabled);
        if (!notifEnabled) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        assert alarmManager != null;

        for (Assignment assignment : FileIO.inProgressAssignments) {
            setNotificationTimer(context, assignment, alarmManager, prefs);
        }

    }

    private static void setNotificationTimer(Context context, Assignment assignment, AlarmManager alarmManager, SharedPreferences prefs) {
        if (!assignment.completed) {
            PendingIntent pendingIntent = createPendingIntent(assignment, context);
            alarmManager.cancel(pendingIntent);

            //"normal" notification setting
            long time = alarmTimeFromAssignment(assignment, prefs, false);
            if (time > System.currentTimeMillis()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            }

            //extra notification setting
            if (prefs.getBoolean(Settings.notif2Enabled, false)) {
                long extraTime = alarmTimeFromAssignment(assignment, prefs, true);
                if (time > System.currentTimeMillis()) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, extraTime, pendingIntent);
                }
            }
        }
    }


    /**
     * returns the date, in milliseconds, at x daysBeforeDueDate
     * at time alarmHourOfDay : alarmMinuteOfDay
     *
     * @param assignment assignment to retrieve date from
     * @return time for alarm to go off, in milliseconds
     */
    private static long alarmTimeFromAssignment(Assignment assignment, SharedPreferences prefs, boolean extraAssignment) {
        int daysBeforeDueDate;
        long notifTime;
        if (extraAssignment) {
            daysBeforeDueDate = prefs.getInt(Settings.notif2DaysBefore, 7);
            notifTime = prefs.getLong(Settings.notif2Time, 46800000L);
        } else {
            daysBeforeDueDate = prefs.getInt(Settings.notif1DaysBefore, 1);
            notifTime = prefs.getLong(Settings.notif1Time, 46800000L);
        }
        Calendar notifCalendar = Calendar.getInstance();
        notifCalendar.setTimeInMillis(notifTime);
        int alarmHourOfDay = notifCalendar.get(Calendar.HOUR_OF_DAY);
        int alarmMinuteOfDay = notifCalendar.get(Calendar.MINUTE);


        SimpleDateFormat format = new SimpleDateFormat("h:mm:ss a, EEE, MM/dd/yy", Locale.US);

        Calendar dueDate = assignment.dueDate;
        Calendar date = new GregorianCalendar();
        date.set(dueDate.get(Calendar.YEAR),
                dueDate.get(Calendar.MONTH),
                dueDate.get(Calendar.DATE) - daysBeforeDueDate);
        date.set(Calendar.HOUR_OF_DAY, alarmHourOfDay);
        date.set(Calendar.MINUTE, alarmMinuteOfDay);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        Log.v(TAG, "date=" + format.format(date.getTime()));

        return date.getTimeInMillis();
    }

    /**
     * Returns an intent which will trigger the notification
     *
     * @param assignment Assignment to display
     * @param context    necessary for system calls
     * @return new PendingIntent with assignment id
     */
    private static PendingIntent createPendingIntent(Assignment assignment, Context context) {
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra(AlarmBroadcastReceiver.ID, assignment.uniqueID);
        intent.putExtra(AlarmBroadcastReceiver.TITLE, assignment.title);
        intent.putExtra(AlarmBroadcastReceiver.TEXT, assignment.className);
        intent.putExtra(AlarmBroadcastReceiver.DUE_DATE, assignment.dueDate.getTimeInMillis());
        intent.setAction(AlarmBroadcastReceiver.ACTION_ALARM);

        return PendingIntent.getBroadcast(context, (int) assignment.uniqueID, intent, 0);
    }
}
