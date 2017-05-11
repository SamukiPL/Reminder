package me.samuki.remainder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends Activity {
    ProgressBar progressBar;
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


        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                System.out.println(millisUntilFinished/600);
            }

            @Override
            public void onFinish() {
                System.out.println("KONIEC!!!");
            }
        }.start();

    }
    public void newAction(View view) {
        Intent addIntent = new Intent(this, ActionActivity.class);
        startActivity(addIntent);
    }
}
