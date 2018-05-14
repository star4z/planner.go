package go.planner.plannergo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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

    static void setNotificationTimers(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("pref_time_enabled", true))
            return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        assert alarmManager != null;

        for (NewAssignment assignment : FileIO.inProgressAssignments) {
            setNotificationTimer(context, assignment, alarmManager, prefs);
        }

    }

    private static void setNotificationTimer(Context context, NewAssignment assignment, AlarmManager alarmManager, SharedPreferences prefs) {
        PendingIntent pendingIntent = AlarmBroadcastReceiver.createPendingIntent(
                assignment, context);
        alarmManager.cancel(pendingIntent);

        //"normal" notification setting
        long time = alarmTimeFromAssignment(assignment, prefs, false);
        if (time > Calendar.getInstance().getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }

        //extra notification setting
        if (prefs.getBoolean("pref_extra_notif_enabled", false)) {
            long extraTime = alarmTimeFromAssignment(assignment, prefs, true);
            if (time > Calendar.getInstance().getTimeInMillis()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, extraTime, pendingIntent);
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
    private static long alarmTimeFromAssignment(NewAssignment assignment, SharedPreferences prefs, boolean extraAssignment) {
        int daysBeforeDueDate;
        long notifTime;
        if (extraAssignment) {
            daysBeforeDueDate = prefs.getInt("pref_notif_days_before_extra", 7);
            notifTime = prefs.getLong("pref_notif_time_extra", 46800000L);
        } else {
            daysBeforeDueDate = prefs.getInt("pref_notif_days_before", 1);
            notifTime = prefs.getLong("pref_notif_time", 46800000L);
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
        Log.v("NotificationAlarms", "date=" + format.format(date.getTime()));

        return date.getTimeInMillis();
    }
}
