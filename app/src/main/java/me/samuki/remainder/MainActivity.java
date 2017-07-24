package me.samuki.remainder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    LinearLayout actionsLayout;
    SimpleDateFormat dateFormat;
    Calendar cal;
    static ManagerDbAdapter database;

    TimerService timerService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        dateFormat = new SimpleDateFormat("d-M-yyyy");
        cal = Calendar.getInstance();
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

                TextView slash = (TextView) actionRow.findViewById(R.id.actionRow_slash);
                slash.setText("/");

                String actionType = action.getType();
                TextView type = (TextView) actionRow.findViewById(R.id.actionRow_type);

                TextView amount = (TextView) actionRow.findViewById(R.id.actionRow_amount);
                long howMuch = action.getAmount();
                long toBeDone = action.getToBeDone();
                LinearLayout actionRowButtons = (LinearLayout) actionRow.findViewById(R.id.actionRow_buttons);
                LinearLayout buttons;

                if (actionType.equals(getString(R.string.time))) {
                    setTimeToTextView(amount, howMuch);
                    buttons = (LinearLayout) getLayoutInflater().inflate(R.layout.time_buttons, null);
                    Button startButton = (Button)buttons.getChildAt(0);
                    type.setText(actionType);
                    type.setVisibility(View.GONE);
                    if(i == timerService.getPosition()) {
                        timerService.setProgressAndButton(progress, startButton);
                        startButton.setText(getString(R.string.stop));
                        setTimeToTextView(progress, timerService.getHowMuch()-timerService.getToBeDone());
                    } else {
                        setTimeToTextView(progress, action.getAmount() - toBeDone);
                        if(toBeDone == 0) {
                            startButton.setVisibility(Button.GONE);
                            Button doneButton = (Button) buttons.findViewById(R.id.actionRow_done);
                            doneButton.setText(getString(R.string.doItAgain));
                        }
                    }
                } else {
                    amount.setText(getString(R.string.amountText, howMuch));
                    progress.setText(getString(R.string.amountText, toBeDone));
                    type.setText(actionType);
                    buttons = (LinearLayout) getLayoutInflater().inflate(R.layout.other_buttons, null);
                    if(toBeDone == howMuch) {
                        LinearLayout buttonsContainer = (LinearLayout) buttons.getChildAt(0);
                        buttonsContainer.setVisibility(View.GONE);
                        Button doneButton = (Button) buttons.findViewById(R.id.actionRow_done);
                        doneButton.setText(getString(R.string.doItAgain));
                    }
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
    //TIME BUTTONS
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
            if(timerService.isRunning()) {
                timerService.setTextToButton(getString(R.string.start));
                stopTimerService();
            }
            startTimerService( name, howMuch, toBeDone,
                                    Integer.parseInt(positionView.getText().toString()));
            timerService.setProgressAndButton(progressView, button);
            button.setText(getString(R.string.stop));
        }
        else if(state.equals(getString(R.string.stop))) {
            stopTimerService();
            button.setText(getString(R.string.start));
        }
    }
    //COUNT BUTTONS
    public void addSomeValue(View view) {
        LinearLayout buttonsContainer = (LinearLayout)view.getParent();
        RelativeLayout actionRow = (RelativeLayout)view.getParent().getParent().getParent().getParent();
        long howMuch = Long.parseLong(((TextView)actionRow.findViewById(R.id.actionRow_howMuch))
                .getText().toString());
        long id = Long.parseLong(((TextView)actionRow.findViewById(R.id.actionRow_positionNumber))
                .getText().toString());
        TextView progressView = (TextView)actionRow.findViewById(R.id.actionRow_progress);
        long progress = Long.parseLong(progressView.getText().toString());
        switch(view.getId()) {
            case R.id.actionRow_one:
                progress++;
                break;
            case R.id.actionRow_ten:
                progress += 10;
                break;
            case R.id.actionRow_hundred:
                progress += 100;
                break;
            default:
        }
        if(progress >= howMuch) {
            progress = howMuch;
            buttonsContainer.setVisibility(View.GONE);
            LinearLayout tmpLayout = (LinearLayout)buttonsContainer.getParent();
            Button doneButton = (Button)tmpLayout.findViewById(R.id.actionRow_done);
            doneButton.setText(getString(R.string.doItAgain));
        }
        updateActionToBeDone(id, progress);
        progressView.setText(getString(R.string.amountText, progress));
    }

    public void doneButton(View view) {
        Button button = (Button) view;
        LinearLayout buttonsLayout = (LinearLayout) button.getParent();
        View startButton = buttonsLayout.getChildAt(0);
        RelativeLayout actionRow = (RelativeLayout) button.getParent().getParent().getParent();
        TextView progressView = (TextView) actionRow.findViewById(R.id.actionRow_progress);
        String type = ((TextView)actionRow.findViewById(R.id.actionRow_type)).getText().toString();

        System.out.println(type);

        long howMuch = Long.parseLong(((TextView) actionRow.findViewById(R.id.actionRow_howMuch))
                .getText().toString());
        long tmpMuch = howMuch;
        long id = Long.parseLong(((TextView) actionRow.findViewById(R.id.actionRow_positionNumber))
                .getText().toString());
        if(id == timerService.getPosition()) {
            stopTimerService();
        }
        if(button.getText().toString().equals(getString(R.string.allDone))) {
            startButton.setVisibility(Button.GONE);
            button.setText(getString(R.string.doItAgain));
        } else {
            howMuch = 0;
            startButton.setVisibility(Button.VISIBLE);
            button.setText(getString(R.string.allDone));
        }
        if(type.equals(getString(R.string.time))) {
            setTimeToTextView(progressView, howMuch);
            updateActionToBeDone(id, tmpMuch - howMuch);
        }
        else {
            progressView.setText(getString(R.string.amountText, howMuch));
            updateActionToBeDone(id, howMuch);
        }
    }

    public void editAction(View view) {
        System.out.println("EDIT THIS!!!");
    }

    public static void updateActionToBeDone(long id, long toBeDone) {
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

    private void startTimerService(String name, long howMuch, long toBeDone, int position) {
        Intent startIntent = new Intent(this, TimerService.class);
        startIntent.putExtra("name", name);
        startIntent.putExtra("howMuch", howMuch);
        startIntent.putExtra("toBeDone", toBeDone);
        startIntent.putExtra("position", position);
        startIntent.setAction("Start");
        startService(startIntent);
    }

    private void stopTimerService() {
        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction("Stop");
        startService(stopIntent);
    }
}
