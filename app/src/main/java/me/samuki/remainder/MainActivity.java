package me.samuki.remainder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    LinearLayout actionsLayout;
    SimpleDateFormat dateFormat;
    Calendar cal;
    static CustomTimerClass customTimerClass;
    static ManagerDbAdapter database;

    TimerService timerService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        dateFormat = new SimpleDateFormat("d-M-yyyy");
        cal = Calendar.getInstance();
        customTimerClass = new CustomTimerClass(this);
        actionsLayout = (LinearLayout)findViewById(R.id.actionsHere);
        database = new ManagerDbAdapter(this);
        //JUST WAIT A SECOND TO CONNECT!!!
        bindService(new Intent(this, TimerService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onRestart() {
        actionsLayout.removeAllViews();
        setEveryAction();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        if(timerService != null)
            unbindService(serviceConnection);
        super.onDestroy();
    }

    private void setEveryAction() {
        database.open();
        int actionsAmount = database.actionCount();
        setActionsLayout(actionsAmount, database);
        database.close();
    }

    private void setActionsLayout(int actionsAmount, ManagerDbAdapter database) {
        for (int i = 1; i <= actionsAmount; i++) {
            ActionTodo action = database.getAction(i);
            if(action.getActive() == 1) {
                RelativeLayout actionRow = (RelativeLayout) getLayoutInflater().inflate(R.layout.action_row, null);

                TextView name = (TextView) actionRow.findViewById(R.id.actionRow_name);
                name.setText(action.getName());

                TextView time = (TextView) actionRow.findViewById(R.id.actionRow_time);
                if (action.getOften().equals("default")) {
                    Date startDate = cal.getTime();
                    Date endDate = new Date();
                    try {
                        endDate = dateFormat.parse(action.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long timeLeft = endDate.getTime() - startDate.getTime();
                    int daysLeft = (int)(TimeUnit.DAYS.convert(timeLeft, TimeUnit.MILLISECONDS) + 1);
                    time.setText(getResources().getQuantityString(R.plurals.daysLeft, daysLeft, daysLeft));
                } else {
                    Date startDate = cal.getTime();
                    Date endDate = cal.getTime();
                    String often = action.getOften();
                    long oftenMulti;
                    String[] oftenOnes = getResources().getStringArray(R.array.one);
                    String[] oftenPlurals = getResources().getStringArray(R.array.plural);

                    if (often.equals(oftenOnes[0]) || often.equals(oftenPlurals[0]))
                        oftenMulti = 1;
                    else if (often.equals(oftenOnes[1]) || often.equals(oftenPlurals[1]))
                        oftenMulti = 7;
                    else if (often.equals(oftenOnes[2]) || often.equals(oftenPlurals[2]))
                        oftenMulti = 30;
                    else
                        oftenMulti = 365;

                    long repeat = action.getRepeat() * oftenMulti;
                    try {
                        startDate = dateFormat.parse(action.getDate());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long timeLeft = (Math.abs(startDate.getTime() - endDate.getTime()));
                    int daysLeft = (int) (repeat - (TimeUnit.DAYS.convert(timeLeft, TimeUnit.MILLISECONDS) % (repeat + 1)));
                    time.setText(getResources().getQuantityString(R.plurals.daysLeft, daysLeft, daysLeft));
                }

                TextView progress = (TextView) actionRow.findViewById(R.id.actionRow_progress);
                progress.setText("0");

                TextView slash = (TextView) actionRow.findViewById(R.id.actionRow_slash);
                slash.setText("/");

                String actionType = action.getType();
                TextView amount = (TextView) actionRow.findViewById(R.id.actionRow_amount);
                long howMuch = action.getAmount();
                LinearLayout actionRowButtons = (LinearLayout) actionRow.findViewById(R.id.actionRow_buttons);
                LinearLayout buttons;
                Button startButton;

                if (actionType.equals(getString(R.string.time))) {
                    setTimeToTextView(amount, howMuch);
                    if(i == timerService.getPosition()) {
                        timerService.setProgress(progress);
                        setTimeToTextView(progress, timerService.getHowMuch()-timerService.getToBeDone());
                    } else setTimeToTextView(progress, action.getAmount() - action.getToBeDone());
                    buttons = (LinearLayout) getLayoutInflater().inflate(R.layout.time_buttons, null);
                } else {
                    amount.setText(getString(R.string.amountText, howMuch));
                    TextView type = (TextView) actionRow.findViewById(R.id.actionRow_type);
                    type.setText(action.getType());
                    buttons = (LinearLayout) getLayoutInflater().inflate(R.layout.other_buttons, null);
                }
                //FOR TIMER
                TextView invisibleView = (TextView) actionRow.findViewById(R.id.actionRow_howMuch);
                invisibleView.setText(getString(R.string.amountText, howMuch));
                TextView toBeDoneView = (TextView) actionRow.findViewById(R.id.actionRow_toBeDone);
                toBeDoneView.setText(getString(R.string.amountText, action.getToBeDone()));
                TextView positionView = (TextView) actionRow.findViewById(R.id.actionRow_positionNumber);
                positionView.setText(getString(R.string.amountText, i));
                actionRowButtons.addView(buttons);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 25);
                actionRow.setLayoutParams(params);
                actionsLayout.addView(actionRow);
            }
        }
    }

    private void setTimeToTextView(TextView textView, long amount) {
        int hours = (int)amount/3600000;
        int minutes = (int)(amount-(hours*3600000))/60000;
        int minuteTens = minutes/10;
        int minuteOnes = minutes - (minuteTens*10);
        int second = (int)(amount-(hours*3600000)-(minutes*60000))/1000;
        if(second != 0) {
            int secondTens = second / 10;
            int secondOnes = second - (secondTens * 10);
            textView.setText(getString(R.string.amountTimeWithSeconds, hours,
                            minuteTens, minuteOnes, secondTens, secondOnes));
        } else {
            textView.setText(getString(R.string.amountTime, hours, minuteTens, minuteOnes));
        }
    }
    public static void setTimeToTextView(Context context, TextView textView, long amount) {
        int hours = (int)amount/3600000;
        int minutes = (int)(amount-(hours*3600000))/60000;
        int minuteTens = minutes/10;
        int minuteOnes = minutes - (minuteTens*10);
        int second = (int)(amount-(hours*3600000)-(minutes*60000))/1000;
        int secondTens = second/10;
        int secondOnes = second - (secondTens*10);
        textView.setText(context.getString(R.string.amountTimeWithSeconds, hours,
                                minuteTens, minuteOnes, secondTens, secondOnes));
    }

    public void newAction(View view) {
        Intent addIntent = new Intent(this, ActionActivity.class);
        startActivity(addIntent);
    }

    public void startTimer(View view) {
        Button button = (Button)view;
        String state = button.getText().toString();
        RelativeLayout actionRow = (RelativeLayout)button.getParent().getParent().getParent();
        TextView positionView = (TextView)actionRow.findViewById(R.id.actionRow_positionNumber);
        TextView progressView = (TextView)actionRow.findViewById(R.id.actionRow_progress);
        database.open();
        ActionTodo action = database.getAction(Integer.parseInt(positionView.getText().toString()));
        String name = action.getName();
        long howMuch = action.getAmount();
        long toBeDone = action.getToBeDone();
        if(state.equals(getString(R.string.start))) {
            customTimerClass.start( name, howMuch, toBeDone,
                                    Integer.parseInt(positionView.getText().toString()));
            customTimerClass.setProgressView(progressView);
            button.setText(getString(R.string.stop));
        }
        else if(state.equals(getString(R.string.stop))) {
            customTimerClass.cancel();
            button.setText(getString(R.string.start));
        }
    }

    public static void updateAction(int id, long toBeDone) {
        System.out.println("TU TEZ!!!");
        database.open();
        ActionTodo action = database.getAction(id);
        action.setToBeDone(toBeDone);
        database.updateAction(action);
        database.close();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
            timerService = binder.getService();
            System.out.println("TAK JEST!!!");
            setEveryAction();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("NIESTETY :(");
        }
    };
}
class CustomTimerClass {

    private Context context;
    private TextView progress;
    long toBeDone;

    CustomTimerClass(Context context) {
        this.context = context;
    }

    void setProgressView(TextView progress) {
        this.progress = progress;
    }
    TextView getProgressView(){
        return progress;
    }

    void start(String name, long howMuch, long toBeDone, int position) {
        Intent startIntent = new Intent(context, TimerService.class);
        startIntent.putExtra("name", name);
        startIntent.putExtra("howMuch", howMuch);
        startIntent.putExtra("toBeDone", toBeDone);
        startIntent.putExtra("position", position);
        startIntent.setAction("Start");
        context.startService(startIntent);
    }

    void cancel() {
        Intent stopIntent = new Intent(context, TimerService.class);
        stopIntent.setAction("Stop");
        context.startService(stopIntent);
    }
}
