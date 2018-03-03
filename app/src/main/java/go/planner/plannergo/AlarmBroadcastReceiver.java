package go.planner.plannergo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.os.Build.VERSION_CODES.O;

/**
 * Receives alarm intent when notification is supposed to be created, and creates the notification.
 * Created by Ben Phillips on 2/16/2018.
 */

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_ALARM = "planner.app.Alarm1";
    public static final String MARK_DONE = "planner.app.MarkDone";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("AlarmBroadcastReceiver", "alarm was received");
        if (ACTION_ALARM.equals(intent.getAction())) {
            Assignment assignment = new Assignment(intent.getExtras());
            int uniqueID = intent.getIntExtra("uniqueID", 1);
            boolean timeEnabled = intent.getBooleanExtra("timeEnabled", false);
            Calendar dueDate = getCalendar(intent);

            createNotification(assignment, uniqueID, dueDate, timeEnabled, context);

        } else if (MARK_DONE.equals(intent.getAction())) {
            Assignment doneAssignment = new Assignment(intent.getExtras());
            Log.v("AlarmBR","doneAssignment=" + doneAssignment);

            //cancel notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.cancel(doneAssignment.hashCode());

//            ArrayList<Assignment> assignments = FileIO.readAssignmentsFromFile(context);
            FileIO.readAssignmentsFromFile(context);
            for (Assignment nextAssignment : FileIO.inProgressAssignments) {
                if (doneAssignment.equals(nextAssignment)) {
                    nextAssignment.completed = true;
                    break;
                }
            }

            FileIO.writeAssignmentsToFile(context);
        }
    }

    Calendar getCalendar(Intent intent) {
        Calendar dueDate = Calendar.getInstance();
        int year = intent.getIntExtra("year", 2000);
        int month = intent.getIntExtra("month", 0);
        int date = intent.getIntExtra("date", 0);
        int hour = intent.getIntExtra("hour", 0);
        int minute = intent.getIntExtra("minute", 0);
        Log.v("ABReceiver", date + " " + month + " " + year + ", " + hour + ":" + minute);
        dueDate.set(year, month, date, hour, minute);
        return dueDate;
    }

    void createNotification(Assignment assignment, int uniqueID, Calendar dueDate, boolean timeEnabled, Context context) {
        SimpleDateFormat dateFormat = timeEnabled ?
                new SimpleDateFormat(" - MMM dd hh:mm", Locale.US) :
                new SimpleDateFormat(" - MMM dd", Locale.US);
        String dateString = dateFormat.format(dueDate.getTime());

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);

        String channelId = "channel_01";
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, 0);
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(2);


        Intent doneIntent = new Intent(context, AlarmBroadcastReceiver.class);
        doneIntent.putExtras(assignment.generateBundle());
        doneIntent.setAction(AlarmBroadcastReceiver.MARK_DONE);

        long[] vibrationPattern = new long[]{100, 100, 200, 100};

        if (Build.VERSION.SDK_INT >= O) {
            String name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.MAGENTA);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(vibrationPattern);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);
        }

        Notification notification = (Build.VERSION.SDK_INT < O) ?
                new NotificationCompat.Builder(context, channelId).
                        setContentTitle(assignment.title).
                        setContentText(assignment.className + dateString).
                        setContentIntent(pendingIntent).setSmallIcon(R.drawable.ic_notification).
                        addAction(new Action(
                                R.drawable.ic_check_default_24dp,
                                "Done",
                                PendingIntent.getBroadcast(context, 0, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT))).
                        setColor(ContextCompat.getColor(context, R.color.colorAccent)).
                        setPriority(NotificationCompat.PRIORITY_HIGH).
                        setSound(notificationSoundUri).
                        setLights(Color.MAGENTA, 100, 10000).
                        setVibrate(vibrationPattern).
                        setAutoCancel(true).
                        build() :
                new NotificationCompat.Builder(context, channelId).
                        setContentTitle(assignment.title).
                        setContentText(assignment.className + dateString).
                        setContentIntent(pendingIntent).setSmallIcon(R.drawable.ic_notification).
                        addAction(new Action(
                                R.drawable.ic_check_default_24dp,
                                "Done",
                                PendingIntent.getBroadcast(context, 0, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT))).
                        setAutoCancel(true).
                        build();

        assert notificationManager != null;
        notificationManager.notify(uniqueID, notification);

    }


    public static PendingIntent createPendingIntent(Assignment assignment, int i, boolean timeEnabled, Context context) {
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtras(assignment.generateBundle());
        intent.putExtra("hour", assignment.dueDate.get(Calendar.HOUR_OF_DAY));
        intent.putExtra("minute", assignment.dueDate.get(Calendar.MINUTE));
        intent.putExtra("uniqueID", assignment.hashCode());
        intent.putExtra("timeEnabled", timeEnabled);
        intent.setAction(AlarmBroadcastReceiver.ACTION_ALARM);

        return PendingIntent.getBroadcast(context, i, intent, 0);
    }

    class NotificationBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}