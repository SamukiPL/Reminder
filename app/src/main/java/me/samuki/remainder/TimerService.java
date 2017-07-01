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
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;


public class TimerService extends Service{
    NotificationManager notificationManager;
    NotificationCompat.Builder notificationBuilder;
    Notification notification;

    CountDownTimer timer;
    long howMuch;
    long toBeDone;

    TimerService(){}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notifyID = 101;

        notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Your Progress")
                .setTicker("Your Progress")
                .setContentText("Progress")
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setOngoing(true);

        if(intent.getAction().equals("Start")) {
            howMuch = (long)intent.getExtras().get("howMuch");
            toBeDone = (long)intent.getExtras().get("toBeDone");

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Intent pauseIntent = new Intent(this, TimerService.class);
            pauseIntent.setAction("Pause");
            PendingIntent ppauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

            Intent stopIntent = new Intent(this, TimerService.class);
            stopIntent.setAction("Stop");
            stopIntent.putExtra("howMuch", howMuch);
            stopIntent.putExtra("toBeDone", 20);
            PendingIntent pstopIntent = PendingIntent.getService(this, 0, stopIntent, 0);

            notificationBuilder.setContentIntent(pendingIntent)
                    .addAction(android.R.drawable.ic_media_pause, "Pause", ppauseIntent)
                    .addAction(R.drawable.cancel, "Stop", pstopIntent).build();
            notification = notificationBuilder.build();
            startForeground(notifyID, notification);

            setTimer();
            timer.start();
        } else if(intent.getAction().equals("Pause")) {
            System.out.println("Pause");
        } else if(intent.getAction().equals("Stop")) {
            timer.cancel();
            System.out.println(intent.getExtras().get("toBeDone"));
            stopForeground(true);
            stopSelf();
        }
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
        timer = new CountDownTimer(toBeDone, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                toBeDone = millisUntilFinished;
                System.out.println(toBeDone);
                notificationBuilder.setContentText(longToTime(howMuch - toBeDone));
                notificationManager.notify(101,notificationBuilder.build());
            }

            @Override
            public void onFinish() {
                notificationBuilder.setContentText(longToTime(howMuch));
                notificationManager.notify(101,notificationBuilder.build());
            }
        };
    }

    public String longToTime(long amount) {
        int hours = (int)amount/3600000;
        int minutes = (int)(amount-(hours*3600000))/60000;
        int minuteTens = minutes/10;
        int minuteOnes = minutes - (minuteTens*10);
        int second = (int)(amount-(hours*3600000)-(minutes*60000))/1000;
        int secondTens = second/10;
        int secondOnes = second - (secondTens*10);
        return getString(R.string.amountTimeWithSeconds, hours, minuteTens, minuteOnes, secondTens, secondOnes);
    }

    public class MyBinder extends Binder {
        public TimerService getServiceSystem() {
            return TimerService.this;
        }
    }
}
