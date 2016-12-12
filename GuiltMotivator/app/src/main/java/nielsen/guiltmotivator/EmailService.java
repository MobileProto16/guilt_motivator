package nielsen.guiltmotivator;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zlan on 12/12/16.
 */
public class EmailService extends Service {

    private static WeakReference<Activity> mActivityRef;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d("lol","service is working");
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Log.d("lollol","hahahaha");
                            checkAllTasks();
                        } catch (Exception e) {
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 60000); //execute in every 10 ms
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android
        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +100, restartServicePI);

    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        //start a separate thread and start listening to your network object
    }

    public static void updateActivity(Activity activity) {
        mActivityRef = new WeakReference<>(activity);
    }

    public void checkAllTasks(){
        Log.d("haha","got here");
        DatabaseHelper mDbHelper = new DatabaseHelper(getApplicationContext());
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //grab arraylist of tasks from the database
        ArrayList<Task> tasks = mDbHelper.getAllTasks();
        Calendar cur = Calendar.getInstance();
        for (int i = 0; i < tasks.size(); i++){
            if (tasks.get(i).getDueDate().compareTo(cur) >= 0 && !tasks.get(i).isChecked()){
                sendEmail(getApplicationContext(),tasks.get(i));
            }
        }
    }

    private void sendEmail(Context context, Task task){
//        Activity activity = (Activity) context;
        //get helper and get db in write mode
        DatabaseHelper mDbHelper = new DatabaseHelper(context);
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ArrayList<Contact> contacts = mDbHelper.getContacts(task);
        Activity activity = mActivityRef.get();
        final SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        final String tone = sharedPref.getString(MainActivity.SAVED_TONE, "polite");
        final String name = sharedPref.getString(MainActivity.SAVED_NAME, "none");
        final String politeMsg = "polite";
        final String profaneMsg = "profane";
//        String msg = tone == "polite"? politeMsg : profaneMsg;
        String msg = name + tone;
        for (int i = 0; i < contacts.size();i++){
            //Creating SendMail object
            SendMail sm = new SendMail(context, contacts.get(i).getAddress(), "From Guilt Motivator", msg);
            //Executing sendmail to send email
            sm.execute();
        }
        task.toggleChecked();
        mDbHelper.editTask(task);
    }
}