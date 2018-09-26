package go.planner.plannergo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.os.Build.VERSION_CODES.O;

/**
 * Receives alarm intent when notification is supposed to be created, and creates the notification.
 * Dismisses notification when "Done" is pressed on notification.
 *
 * There are some unregulated long to int casts, which may produce incorrect calls in unlikely cases.
 *
 * Created by Ben Phillips on 2/16/2018.
 */

public class AlarmBroadcastReceiver extends BroadcastReceiver {


    public static final String ACTION_ALARM = "planner.app.Alarm1";
    public static final String MARK_DONE = "planner.app.MarkDone";

    /**
     * Handles creation and dismissal of notifications
     *
     * @param context needed for system calls
     * @param intent contains data for action to be taken and the assignment
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("AlarmBroadcastReceiver", "alarm was received");
        if (ACTION_ALARM.equals(intent.getAction())) {
            NewAssignment assignment = FileIO.getAssignment(intent.getLongExtra("id", -1L));
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean timeEnabled = preferences.getBoolean(Settings.timeEnabled, true);

            createNotification(assignment, timeEnabled, context);

        } else if (MARK_DONE.equals(intent.getAction())) {
            NewAssignment doneAssignment = FileIO.getAssignment(intent.getLongExtra("id", -1L));
            Log.v("AlarmBR","doneAssignment=" + doneAssignment);

            //cancel notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.cancel((int) doneAssignment.uniqueID);

            FileIO.readFiles(context);
            doneAssignment.completed = true;
            FileIO.replaceAssignment(context, doneAssignment);
            FileIO.writeFiles(context);
        }
    }


    void createNotification(NewAssignment assignment, boolean timeEnabled, Context context) {
        SimpleDateFormat dateFormat = timeEnabled ?
                new SimpleDateFormat(" - MMM dd hh:mm", Locale.US) :
                new SimpleDateFormat(" - MMM dd", Locale.US);
        String dateString = dateFormat.format(assignment.dueDate.getTime());

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);

        String channelId = "channel_01";
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, 0);
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(2);


        Intent doneIntent = new Intent(context, AlarmBroadcastReceiver.class);
        doneIntent.putExtra("id", assignment.uniqueID);
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
                                context.getString(R.string.done),
                                PendingIntent.getBroadcast(context, 0, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT))).
                        setAutoCancel(true).
                        build();

        assert notificationManager != null;
        notificationManager.notify(((int) assignment.uniqueID), notification);

    }



}