package go.planner.plannergo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
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
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Bundle settings = FileIO.readSettings(context);
        Log.v("NotificationAlarms","alarmHour=" + settings.getInt("alarmHour")
                + "alarmMinute=" + settings.getInt("alarmMinute"));

        for (Assignment assignment : FileIO.inProgressAssignments) {
            setNotificationTimer(context, assignment, alarmManager, settings);
        }

    }

    static void setNotificationTimer(Context context, Assignment assignment, AlarmManager alarmManager, Bundle settings) {
        PendingIntent pendingIntent = AlarmBroadcastReceiver.createPendingIntent(
                assignment, assignment.hashCode(), settings.getBoolean("timeEnabled"), context);
        alarmManager.cancel(pendingIntent);
        long time = alarmTimeFromAssignment(assignment, settings);
        if (time > Calendar.getInstance().getTimeInMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }

    /**
     * returns the date, in milliseconds, at x daysBeforeDueDate
     * at time alarmHourOfDay : alarmMinuteOfDay
     *
     * @param assignment assignment to retrieve date from
     * @return time for alarm to go off, in milliseconds
     */
    static long alarmTimeFromAssignment(Assignment assignment, Bundle settings) {
        int daysBeforeDueDate = settings.getInt("daysBeforeDueDate", 1);
        int alarmHourOfDay = settings.getInt("alarmHour", 8);
        int alarmMinuteOfDay = settings.getInt("alarmMinute", 0);


        SimpleDateFormat format = new SimpleDateFormat("h:mm:ss a, EEE, MM/dd/yy", Locale.US);

        Calendar dueDate = assignment.dueDate;
        Log.v("NotificationAlarms", "assignment.dueDate=" + format.format(assignment.dueDate.getTime()));
        Calendar date = new GregorianCalendar();
        date.set(dueDate.get(Calendar.YEAR),
                dueDate.get(Calendar.MONTH),
                dueDate.get(Calendar.DATE) - daysBeforeDueDate);
        date.set(Calendar.HOUR_OF_DAY, alarmHourOfDay);
        date.set(Calendar.MINUTE, alarmMinuteOfDay);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        Log.v("NotificationAlarms", "date=" + format.format(date.getTime()));

//
//        long notificationDate = settings.getLong("notificationDate", 0);
//        Log.v("NotificationAlarms", "notificationDate=" + format.format(notificationDate));
//        Log.v("NotificationAlarms", "notificationDate=" + notificationDate);
//
//        long dateL = assignment.dueDate.getTimeInMillis();
//        Log.v("NotificationAlarms", "dateLong=" + format.format(dateL));
//
//        long date = dateL - notificationDate;
//        Log.v("NotificationAlarms", "date=" + format.format(date));

//        dateL -= (daysBeforeDueDate * 24 * 60 * 60 * 1000);
//        Log.v("NotificationAlarms", "long=" + format.format(dateL));
//        long currentHour = dateL / 60/60/1000;
//        Log.v("NotificationAlarms", "currentH=" + format.format(currentHour));
//        long currentDay = dateL / 24 / 60 / 60 / 1000;
//        Log.v("NotificationAlarms", "currentD=" + format.format(currentDay));

        return date.getTimeInMillis();
    }
}
