package nielsen.guiltmotivator;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by DHZ_Bill on 11/29/16.
 */
public class NotificationEventReceiver extends WakefulBroadcastReceiver{

    private static final String ACTION_START_NOTIFICATION_SERVICE = "ACTION_START_NOTIFICATION_SERVICE";
    private static final String ACTION_DELETE_NOTIFICATION = "ACTION_DELETE_NOTIFICATION";

    @SuppressLint("NewApi")
    public static void setupAlarm(Context context) {
        //get helper and get db in write mode
        DatabaseHelper mDbHelper = new DatabaseHelper(context);
        //grab arraylist of tasks from the database
        ArrayList<Task> tasks = mDbHelper.getAllTasks();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        ArrayList<PendingIntent> intentArray = new ArrayList<PendingIntent>();
        for(int i = 0; i < tasks.size(); i++) {
            PendingIntent alarmIntent = getStartPendingIntent(context,i);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    getTriggerAt(tasks.get(i).getDueDate().getTime()),
                    alarmIntent);
            intentArray.add(alarmIntent);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent serviceIntent = null;
        if (ACTION_START_NOTIFICATION_SERVICE.equals(action)) {
            Log.i(getClass().getSimpleName(), "onReceive from alarm, starting notification service");
            serviceIntent = NotificationIntentService.createIntentStartNotificationService(context);
        } else if (ACTION_DELETE_NOTIFICATION.equals(action)) {
            Log.i(getClass().getSimpleName(), "onReceive delete notification action, starting notification service to handle delete");
            serviceIntent = NotificationIntentService.createIntentDeleteNotification(context);
        }
        if (serviceIntent != null) {
            startWakefulService(context, serviceIntent);
        }
    }

    private static long getTriggerAt(Date dueDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dueDate);
        return calendar.getTimeInMillis();
    }

    private static PendingIntent getStartPendingIntent(Context context, int i) {
        Intent intent = new Intent(context, NotificationEventReceiver.class);
        intent.setAction(ACTION_START_NOTIFICATION_SERVICE);
        return PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getDeleteIntent(Context context) {
        Intent intent = new Intent(context, NotificationEventReceiver.class);
        intent.setAction(ACTION_DELETE_NOTIFICATION);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
