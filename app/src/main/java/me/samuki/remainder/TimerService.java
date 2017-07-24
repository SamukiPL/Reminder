package me.samuki.remainder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class TimerService extends Service{
    private final IBinder mBinder = new LocalBinder();
    private boolean running;

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
    private Button startButton;

    TimerService(){}

    boolean isRunning() {return running;}
    long getHowMuch() {
        return howMuch;
    }
    long getToBeDone() {
        return toBeDone;
    }
    int getPosition() {
        return position;
    }
    void setProgressAndButton(TextView progress, Button startButton) {
        this.progress = progress;
        this.startButton = startButton;
    }
    void setTextToButton(String text) {startButton.setText(text);}

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
                PendingIntent pStopIntent = PendingIntent.getService(this, 0, stopIntent, 0);

                if (intent.getAction().equals("Start") || intent.getAction().equals("StartAfterPause")) { //START
                    if (intent.getAction().equals("Start")) {
                        name = (String) intent.getExtras().get("name");
                        howMuch = (long) intent.getExtras().get("howMuch");
                        toBeDone = (long) intent.getExtras().get("toBeDone");
                        position = (int) intent.getExtras().get("position");
                    }

                    Intent pauseIntent = new Intent(this, TimerService.class);
                    pauseIntent.setAction("Pause");
                    PendingIntent pPauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

                    notificationBuilder.setContentIntent(pendingIntent)
                            .setContentTitle(name)
                            .setTicker(name)
                            .addAction(android.R.drawable.ic_media_pause, "Pause", pPauseIntent)
                            .addAction(R.drawable.cancel, "Stop", pStopIntent).build();
                    notification = notificationBuilder.build();
                    startForeground(notifyID, notification);

                    setTimer();
                    timer.start();
                    running = true;
                    startButton.setText(getString(R.string.stop));
                } else if (intent.getAction().equals("Pause")) { //PAUSE
                    long amount = (howMuch - toBeDone);
                    startButton.setText(getString(R.string.start));
                    timer.cancel();

                    MainActivity.updateActionToBeDone(position, toBeDone);

                    Intent pauseIntent = new Intent(this, TimerService.class);
                    pauseIntent.setAction("StartAfterPause");
                    PendingIntent pPauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);

                    notification = notificationBuilder.setContentIntent(pendingIntent)
                            .setContentTitle(name)
                            .setTicker(name)
                            .addAction(android.R.drawable.ic_media_play, "Pause", pPauseIntent)
                            .addAction(R.drawable.cancel, "Stop", pStopIntent)
                            .setContentText(getString(R.string.spendMoreTime,
                                    longToTime(howMuch - amount))).build();

                    notificationManager.notify(notifyID, notification);
                } else if (intent.getAction().equals("Stop")) { //STOP
                    startButton.setText(getString(R.string.start));
                    MainActivity.updateActionToBeDone(position, toBeDone);
                    timer.cancel();
                    running = false;
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
        timer = new CountDownTimer(toBeDone, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                toBeDone = Math.round(millisUntilFinished/500) * 500;
                System.out.println(toBeDone);
                long amount = (howMuch - toBeDone);
                notificationBuilder.setContentText(getString(R.string.spendMoreTime, longToTime(howMuch - amount)));
                notificationManager.notify(101,notificationBuilder.build());
                MainActivity.setTimeToTextView(context, progress, amount);
            }

            @Override
            public void onFinish() {
                MainActivity.setTimeToTextView(context, progress, howMuch);
                MainActivity.updateActionToBeDone(position, toBeDone);
                startButton.setVisibility(Button.GONE);
                LinearLayout buttonsLayout = (LinearLayout)startButton.getParent();
                Button secondButton = (Button) buttonsLayout.findViewById(R.id.actionRow_done);
                secondButton.setText(getString(R.string.doItAgain));
                Vibrator vibrator =(Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(500);
                endNotify(context);
                stopForeground(true);
                stopSelf();
            }
        };
    }

   private void endNotify(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(context)
                .setContentIntent(pendingIntent)
                .setContentTitle(name)
                .setContentText(getString(R.string.youAreDone))
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
