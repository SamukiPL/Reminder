package me.samuki.remainder;

import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    LinearLayout actionsLayout;
    SimpleDateFormat dateFormat;
    Calendar cal;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        cal = Calendar.getInstance();

        actionsLayout = (LinearLayout)findViewById(R.id.actionsHere);
        ManagerDbAdapter database = new ManagerDbAdapter(this);
        database.open();
        int actionsAmount = database.actionCount();
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
            if(actionType.equals("Time")) {
                long howMuchTime = action.getAmount();
                int hours = (int)howMuchTime/3600000;
                int minutes = (int)(howMuchTime-(hours*3600000))/60000;
                int minuteTens = minutes/10;
                int minuteOnes = minutes - (minuteTens*10);
                amount.setText(getString(R.string.amountTime, hours, minuteTens, minuteOnes));
                progress.setText(getString(R.string.amountTime, 0,0,0));
            }
            else {
                amount.setText(getString(R.string.amountText, action.getAmount()));
                TextView type =(TextView) actionRow.getChildAt(6);
                type.setText(action.getType());
            }


            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,0,0,25);
            actionRow.setLayoutParams(params);
            actionsLayout.addView(actionRow);
        }
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
    public void newAction(View view) {
        Intent addIntent = new Intent(this, ActionActivity.class);
        startActivity(addIntent);
    }
}
