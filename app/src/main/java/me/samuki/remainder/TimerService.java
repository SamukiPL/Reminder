package me.samuki.remainder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.TextView;
import android.widget.Toast;


public class TimerService extends Service{

    MyBinder binder = new MyBinder();
    private Context context;
    private TextView progress;
    private int listId;
    private CountDownTimer timer;
    private long howMuch;
    long toBeDone;

    TimerService(){}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ManagerDbAdapter database = new ManagerDbAdapter(this);
        database.open();
        System.out.println(database.getAction(2).getAmount());
        progress = MainActivity.textViewList.get(startId-1);
        Long[] longs = MainActivity.longList.get(startId-1);
        howMuch = longs[0];
        toBeDone = longs[1];
        listId = startId;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "STARTED", Toast.LENGTH_SHORT).show();
        setTimer();
        timer.start();
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    private void setTimer() {
        timer = new CountDownTimer(toBeDone, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                System.out.println(toBeDone);
                toBeDone = millisUntilFinished;
                MainActivity.setTimeToTextView(context, progress, Math.abs(howMuch - toBeDone));
            }

            @Override
            public void onFinish() {
                MainActivity.setTimeToTextView(context, progress, Math.abs(howMuch));
            }
        };
    }

    public class MyBinder extends Binder {
        public TimerService getServiceSystem() {
            return TimerService.this;
        }
    }
}
