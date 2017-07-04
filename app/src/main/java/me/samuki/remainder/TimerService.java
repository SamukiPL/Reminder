package me.samuki.remainder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Time;


public class TimerService extends Service{
    private final IBinder mBinder = new LocalBinder();

    NotificationManager notificationManager;
    NotificationCompat.Builder notificationBuilder;
    Notification notification;

    private CountDownTimer timer;
    private String name;
    private long howMuch;
    private long toBeDone;
    private int position;
    private Context context;
    private TextView progress;

    TimerService(){}

    long getHowMuch() {
        return howMuch;
    }
    long getToBeDone() {
        return toBeDone;
    }
    int getPosition() {
        return position;
    }
    void setProgress(TextView progress) {
        this.progress = progress;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (!intent.getAction().equals("Binding")) {
                context = getApplicationContext();
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                int notifyID = 101;

                notificationBuilder = new NotificationCompat.Builder(this)
                        .setContentText("Progress")
                        .setSmallIcon(R.drawable.ic_launcher_round)
                        .setOngoing(true);
                //NOTIFICATION INTENT
                Intent notificationIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
                //STOP INTENT
                Intent stopIntent = new Intent(this, TimerService.class);
                stopIntent.setAction("Stop");
                PendingIntent pstopIntent = PendingIntent.getService(this, 0, stopIntent, 0);

                if (intent.getAction().equals("Start") || intent.getAction().equals("StartAfterPause")) {
                    if (intent.getAction().equals("Start")) {
                        name = (String) intent.getExtras().get("name");
                        howMuch = (long) intent.getExtras().get("howMuch");
                        toBeDone = (long) intent.getExtras().get("toBeDone");
                        position = (int) intent.getExtras().get("position");
                        progress = MainActivity.customTimerClass.getProgressView();
                    }

                    Intent pauseIntent = new Intent(this, TimerService.class);
                    pauseIntent.setAction("Pause");
                    PendingIntent ppauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

                    notificationBuilder.setContentIntent(pendingIntent)
                            .setContentTitle(name)
                            .setTicker(name)
                            .addAction(android.R.drawable.ic_media_pause, "Pause", ppauseIntent)
                            .addAction(R.drawable.cancel, "Stop", pstopIntent).build();
                    notification = notificationBuilder.build();
                    startForeground(notifyID, notification);

                    setTimer();
                    timer.start();
                } else if (intent.getAction().equals("Pause")) {
                    System.out.println(toBeDone);
                    timer.cancel();

                    Intent pauseIntent = new Intent(this, TimerService.class);
                    pauseIntent.setAction("StartAfterPause");
                    PendingIntent ppauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

                    notification = notificationBuilder.setContentIntent(pendingIntent)
                            .setContentTitle(name)
                            .setTicker(name)
                            .addAction(android.R.drawable.ic_media_play, "Pause", ppauseIntent)
                            .addAction(R.drawable.cancel, "Stop", pstopIntent)
                            .setContentText(longToTime(howMuch - toBeDone)).build();

                    notificationManager.notify(notifyID, notification);
                } else if (intent.getAction().equals("Stop")) {
                    System.out.println("DZIALA!!!");
                    MainActivity.updateAction(position, toBeDone);
                    timer.cancel();
                    stopForeground(true);
                    stopSelf();
                }
            }
        } catch (NullPointerException ignored) {}
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setTimer() {
        System.out.println(howMuch+"\n"+toBeDone);
        timer = new CountDownTimer(toBeDone+1000, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                toBeDone = Math.round(millisUntilFinished/1000)*1000;
                System.out.println(toBeDone);
                long amount = howMuch - toBeDone;
                notificationBuilder.setContentText(longToTime(amount));
                notificationManager.notify(101,notificationBuilder.build());
                MainActivity.setTimeToTextView(context, progress, amount);
            }

            @Override
            public void onFinish() {
                MainActivity.setTimeToTextView(context, progress, howMuch);
                MainActivity.updateAction(position, toBeDone);
                Vibrator vibrator =(Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(500);
                stopForeground(true);
                stopSelf();
                endNotify(context);
            }
        };
    }

   private void endNotify(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setContentTitle(name)
                .setContentText("You are done with your task")
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setOngoing(false)
                .setAutoCancel(true).build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    String longToTime(long amount) {
        int hours = (int)amount/3600000;
        int minutes = (int)(amount-(hours*3600000))/60000;
        int minuteTens = minutes/10;
        int minuteOnes = minutes - (minuteTens*10);
        int second = (int)(amount-(hours*3600000)-(minutes*60000))/1000;
        int secondTens = second/10;
        int secondOnes = second - (secondTens*10);
        return getString(R.string.amountTimeWithSeconds, hours, minuteTens, minuteOnes, secondTens, secondOnes);
    }

    public class LocalBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }
}
