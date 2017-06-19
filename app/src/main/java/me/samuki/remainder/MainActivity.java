package me.samuki.remainder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    List<CountDownTimer> timerList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        cal = Calendar.getInstance();
        timerList = new ArrayList<>();

        actionsLayout = (LinearLayout)findViewById(R.id.actionsHere);
        ManagerDbAdapter database = new ManagerDbAdapter(this);
        database.open();
        int actionsAmount = database.actionCount();
        setActionsLayout(actionsAmount, database);
        /*
        new CountDownTimer(database.getAction(actionsAmount).getAmount(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                System.out.println(millisUntilFinished/600);
            }

            @Override
            public void onFinish() {
                System.out.println("KONIEC!!!");
            }
        }.start();*/
        database.close();
    }
    private void setActionsLayout(int actionsAmount, ManagerDbAdapter database) {
        for (int i = 1; i <= actionsAmount; i++) {
            RelativeLayout actionRow = (RelativeLayout)getLayoutInflater().inflate(R.layout.action_row, null);
            ActionTodo action = database.getAction(i);

            TextView name =(TextView) actionRow.getChildAt(0);
            name.setText(action.getName());

            TextView time =(TextView) actionRow.getChildAt(1);
            if(action.getOften().equals("default")) {
                Date startDate = cal.getTime();
                Date endDate = new Date();
                try {
                    endDate = dateFormat.parse(action.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long timeLeft = endDate.getTime() - startDate.getTime();
                time.setText(getString(R.string.amountText, TimeUnit.DAYS.convert(timeLeft, TimeUnit.MILLISECONDS)));
            }
            else {
                Date startDate = cal.getTime();
                Date endDate = cal.getTime();
                String often = action.getOften();
                long oftenMulti;
                String[] oftenOnes = getResources().getStringArray(R.array.one);
                String[] oftenPlurals = getResources().getStringArray(R.array.plural);

                if(often.equals(oftenOnes[0]) || often.equals(oftenPlurals[0]))
                    oftenMulti = 1;
                else if(often.equals(oftenOnes[1]) || often.equals(oftenPlurals[1]))
                    oftenMulti = 7;
                else if(often.equals(oftenOnes[2]) || often.equals(oftenPlurals[2]))
                    oftenMulti = 30;
                else
                    oftenMulti = 365;

                long repeat = action.getRepeat()*oftenMulti;
                try {
                    startDate = dateFormat.parse(action.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                System.out.println(startDate +"     TAK   " + endDate);
                long timeLeft = ( Math.abs(startDate.getTime() - endDate.getTime()));
                long daysLeft = repeat - (TimeUnit.DAYS.convert(timeLeft, TimeUnit.MILLISECONDS)%repeat);
                time.setText(getString(R.string.amountText, daysLeft));
            }

            TextView progress =(TextView) actionRow.getChildAt(3);
            progress.setText("0");

            TextView slash =(TextView) actionRow.getChildAt(4);
            slash.setText("/");

            String actionType = action.getType();
            TextView amount = (TextView) actionRow.getChildAt(5);
            long howMuch = action.getAmount();
            LinearLayout actionRowButtons = (LinearLayout) actionRow.getChildAt(7);
            LinearLayout buttons;
            Button startButton;
            //FOR TIMER
            TextView invisibleView = (TextView)actionRow.getChildAt(8);
            invisibleView.setText(getString(R.string.amountText, howMuch));
            TextView doneView = (TextView)actionRow.getChildAt(9);
            TextView positionView = (TextView)actionRow.getChildAt(10);

            if(actionType.equals("Time")) {
                setTimeToTextView(amount, howMuch);
                progress.setText(getString(R.string.amountTime, 0,0,0));
                buttons = (LinearLayout) getLayoutInflater().inflate(R.layout.time_buttons, null);
                startButton = (Button)buttons.getChildAt(0);
                setTimer(progress, howMuch, startButton, doneView);
                positionView.setText(getString(R.string.amountText, timerList.size()-1));
            }
            else {
                amount.setText(getString(R.string.amountText, howMuch));
                TextView type =(TextView) actionRow.getChildAt(6);
                type.setText(action.getType());
                buttons  = (LinearLayout) getLayoutInflater().inflate(R.layout.other_buttons, null);
            }
            actionRowButtons.addView(buttons);


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,0,0,25);
            actionRow.setLayoutParams(params);
            actionsLayout.addView(actionRow);

        }

    }

    private void setTimeToTextView(TextView textView, long amount) {
        int hours = (int)amount/3600000;
        int minutes = (int)(amount-(hours*3600000))/60000;
        int minuteTens = minutes/10;
        int minuteOnes = minutes - (minuteTens*10);
        textView.setText(getString(R.string.amountTime, hours, minuteTens, minuteOnes));
    }
    private void setTimer(final TextView progress, final long howMuch, final Button button, final TextView doneView) {
        timerList.add(new CountDownTimer(howMuch, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                setTimeToTextView(progress, Math.abs(millisUntilFinished-howMuch));
                doneView.setText(getString(R.string.amountText, Math.abs(millisUntilFinished)));
            }

            @Override
            public void onFinish() {
                setTimeToTextView(progress, Math.abs(howMuch));
                button.setVisibility(View.GONE);
            }
        });
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
        TextView positionView = (TextView)actionRow.getChildAt(9);
        long howMuch = Long.parseLong(howMuchView.getText().toString());
        if(state.equals(getString(R.string.start))) {
            button.setText(getString(R.string.stop));
            timerList.get(Integer.parseInt(positionView.getText().toString())).start();
        }
        else if(state.equals(getString(R.string.stop))) {
            button.setText(getString(R.string.start));
            timerList.get(Integer.parseInt(positionView.getText().toString())).cancel();
        }
    }
}
