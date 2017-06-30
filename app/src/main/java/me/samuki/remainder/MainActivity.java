package me.samuki.remainder;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    LinearLayout actionsLayout;
    SimpleDateFormat dateFormat;
    Calendar cal;
    List<CustomTimerClass> timerList;
    long toBeDone[];

    public static List<TextView> textViewList;
    public static List<Long[]> longList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        dateFormat = new SimpleDateFormat("d-M-yyyy");
        cal = Calendar.getInstance();
        timerList = new ArrayList<>();
        textViewList = new ArrayList<>();
        longList = new ArrayList<>();

        actionsLayout = (LinearLayout)findViewById(R.id.actionsHere);
        ManagerDbAdapter database = new ManagerDbAdapter(this);
        database.open();
        int actionsAmount = database.actionCount();
        toBeDone = new long[actionsAmount];
        setActionsLayout(actionsAmount, database);
        database.close();
    }
    private void setActionsLayout(int actionsAmount, ManagerDbAdapter database) {
        for (int i = 1; i <= actionsAmount; i++) {
            ActionTodo action = database.getAction(i);
            if(action.getActive() == 1) {
                RelativeLayout actionRow = (RelativeLayout) getLayoutInflater().inflate(R.layout.action_row, null);

                TextView name = (TextView) actionRow.getChildAt(0);
                name.setText(action.getName());

                TextView time = (TextView) actionRow.getChildAt(1);
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
                    System.out.println(startDate + "     TAK   " + endDate);
                    long timeLeft = (Math.abs(startDate.getTime() - endDate.getTime()));
                    int daysLeft = (int) (repeat - (TimeUnit.DAYS.convert(timeLeft, TimeUnit.MILLISECONDS) % (repeat + 1)));
                    time.setText(getResources().getQuantityString(R.plurals.daysLeft, daysLeft, daysLeft));
                }

                TextView progress = (TextView) actionRow.getChildAt(3);
                progress.setText("0");

                TextView slash = (TextView) actionRow.getChildAt(4);
                slash.setText("/");

                String actionType = action.getType();
                TextView amount = (TextView) actionRow.getChildAt(5);
                long howMuch = action.getAmount();
                LinearLayout actionRowButtons = (LinearLayout) actionRow.getChildAt(7);
                LinearLayout buttons;
                Button startButton;
                //FOR TIMER
                TextView invisibleView = (TextView) actionRow.getChildAt(8);
                invisibleView.setText(getString(R.string.amountText, howMuch));
                TextView positionView = (TextView) actionRow.getChildAt(10);

                if (actionType.equals(getString(R.string.time))) {
                    setTimeToTextView(amount, howMuch);
                    progress.setText(getString(R.string.amountTime, 0, 0, 0));
                    buttons = (LinearLayout) getLayoutInflater().inflate(R.layout.time_buttons, null);
                    startButton = (Button) buttons.getChildAt(0);
                    positionView.setText(getString(R.string.amountText, timerList.size() - 1));
                    toBeDone[i-1] = howMuch;
                    timerList.add(new CustomTimerClass(this, progress, startButton, howMuch, toBeDone[i-1]));
                } else {
                    amount.setText(getString(R.string.amountText, howMuch));
                    TextView type = (TextView) actionRow.getChildAt(6);
                    type.setText(action.getType());
                    buttons = (LinearLayout) getLayoutInflater().inflate(R.layout.other_buttons, null);
                    toBeDone[i-1] = howMuch;
                }
                actionRowButtons.addView(buttons);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        textView.setText(getString(R.string.amountTime, hours, minuteTens, minuteOnes));
    }
    public static void setTimeToTextView(Context context, TextView textView, long amount) {
        int hours = (int)amount/3600000;
        int minutes = (int)(amount-(hours*3600000))/60000;
        int minuteTens = minutes/10;
        int minuteOnes = minutes - (minuteTens*10);
        String tak = context.getString(R.string.amountTime, hours, minuteTens, minuteOnes);
        textView.setText(tak);
    }

    public void newAction(View view) {
        Intent addIntent = new Intent(this, ActionActivity.class);
        startActivity(addIntent);
    }

    public void startTimer(View view) {
        Button button = (Button)view;
        String state = button.getText().toString();
        RelativeLayout actionRow = (RelativeLayout)button.getParent().getParent().getParent();
        TextView howMuchView = (TextView)actionRow.getChildAt(8);
        TextView positionView = (TextView)actionRow.getChildAt(10);
        if(state.equals(getString(R.string.start))) {
            button.setText(getString(R.string.stop));
            timerList.get(Integer.parseInt(positionView.getText().toString())+1).start();
        }
        else if(state.equals(getString(R.string.stop))) {
            button.setText(getString(R.string.start));
            timerList.get(Integer.parseInt(positionView.getText().toString())+1).cancel();
        }
    }
}
class CustomTimerClass {

    private Context context;
    private TextView progress;
    private Button button;
    private long howMuch;
    long toBeDone;
    Long[] longs;
    private TimerService timerService;
    private Intent timerIntent;

    CustomTimerClass(Context context, TextView view, Button button, long howMuch, long toBeDone) {
        this.context = context;
        this.progress = view;
        this.button = button;
        this.howMuch = howMuch;
        this.toBeDone = toBeDone;

        timerService = new TimerService();
        timerIntent = new Intent(context, timerService.getClass());
        timerIntent.putExtra("howMuch", howMuch);
        timerIntent.putExtra("toBeDone", toBeDone);

        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                TimerService.MyBinder binder = (TimerService.MyBinder)service;
                timerService = binder.getServiceSystem();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    void start() {
        MainActivity.textViewList.add(progress);
        longs = new Long[]{howMuch, toBeDone};
        MainActivity.longList.add(longs);
        context.startService(timerIntent);
        
    }

    void cancel() {
        context.stopService(timerIntent);
        MainActivity.textViewList.remove(progress);
        MainActivity.longList.remove(longs);
    }

    long getToBeDone() {
        return timerService.toBeDone;
    }
}
