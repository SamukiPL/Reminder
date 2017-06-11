package me.samuki.remainder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends Activity {
    LinearLayout actionsLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        SimpleDateFormat tak = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 15);
        cal.set(Calendar.MONTH, 10);
        cal.set(Calendar.YEAR, 1996);
        Date d = cal.getTime();
        String date = tak.format(d);

        actionsLayout = (LinearLayout)findViewById(R.id.actionsHere);
        ManagerDbAdapter database = new ManagerDbAdapter(this);
        database.open();
        int actionsAmount = database.actionCount();
        for (int i = 1; i <= actionsAmount; i++) {
            RelativeLayout nie = (RelativeLayout)getLayoutInflater().inflate(R.layout.action_row, null);

            TextView name =(TextView) nie.getChildAt(0);
            name.setText(database.getAction(i).getName());

            TextView time =(TextView) nie.getChildAt(1);
            time.setText(database.getAction(i).getDate());

            TextView options =(TextView) nie.getChildAt(2);
            options.setText("...");

            TextView progress =(TextView) nie.getChildAt(3);
            progress.setText("0");

            TextView slash =(TextView) nie.getChildAt(4);
            slash.setText("/");

            TextView amount =(TextView) nie.getChildAt(5);
            amount.setText(getString(R.string.amountText, database.getAction(i).getAmount()));

            TextView type =(TextView) nie.getChildAt(6);
            type.setText(database.getAction(i).getType());

            actionsLayout.addView(nie);
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
