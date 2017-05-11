package me.samuki.remainder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ActionActivity extends Activity {
    private LinearLayout typeLayout;

    private Spinner typeSpinner;
    private int typeNumber;
    private TextView dateText;
    //Data format
    private SimpleDateFormat dateFormat;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_activity);

        typeLayout = (LinearLayout) findViewById(R.id.typeLayout);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setTimeLayout();
        setSpinner();
        setDateText();

    }
    private void setSpinner() {
        typeSpinner = (Spinner) findViewById(R.id.actionSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.quantity, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeInputType(parent.getItemAtPosition(position).toString());
                typeNumber = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void changeInputType(String type) {
        if(type.equals("Time"))
            setTimeLayout();
        else {
            setOtherTypeLayout(type);
        }
    }
    private void setTimeLayout() {
        typeLayout.removeAllViews();
        RelativeLayout hoursLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.time_layout, null);
        typeLayout.addView(hoursLayout);

        final EditText hours = (EditText) findViewById(R.id.hours);
        final EditText minutes = (EditText) findViewById(R.id.minutes);
        hours.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count == 1)
                    zeroFirst(hours, s.charAt(0));
                maxLengthCheck(hours, minutes, 2);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        hours.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    timeEmpty(hours);
                    minutes.requestFocus();
                }
                return false;
            }
        });
        minutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                timeEmpty(hours);
                if(count == 1)
                    zeroFirst(minutes, s.charAt(0));
                maxLengthCheck(minutes, 2);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        minutes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                doneButton(minutes, actionId);
                timeEmpty(hours);
                return false;
            }
        });
    }
    private void setOtherTypeLayout(String type) {
        typeLayout.removeAllViews();
        LinearLayout otherLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.others_layout, null);
        typeLayout.addView(otherLayout);
        TextView otherText = (TextView) findViewById(R.id.otherTypeText);
        otherText.setText(type);
        final EditText otherEdit = (EditText) findViewById(R.id.otherType);

        otherEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count == 1)
                    zeroFirst(otherEdit, s.charAt(0));
                maxLengthCheck(otherEdit, 5);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otherEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                doneButton(otherEdit, actionId);
                return false;
            }
        });
    }
    private void timeEmpty(EditText edit) {
        if(edit.getText().toString().equals("")) {
            edit.setText("0");
            System.out.println("OMG");
        }
    }
    private void zeroFirst(EditText edit, char first) {
        if(first == '0' && edit.isFocused())
            edit.setText("");
    }
    private void doneButton(EditText edit, int actionId) {
        if(actionId == EditorInfo.IME_ACTION_DONE) {
            edit.clearFocus();
            timeEmpty(edit);
            InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
        }
    }
    private void maxLengthCheck(EditText edit, EditText nextEdit, int length) {
        if(edit.getText().length() == length) {
            nextEdit.requestFocus();
        }
    }
    private void maxLengthCheck(EditText edit, int length) {
            if(edit.getText().length() == length) {
                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                edit.clearFocus();
            }
    }
    private void setDateText() {
        dateText = (TextView) findViewById(R.id.dateText);
        dateFormat = new SimpleDateFormat("d-M-yyyy");
        dateText.setText(dateFormat.format(new Date().getTime()));
    }
    public void calendarPopup(View view) {
        LinearLayout calendarLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.calendar_alert, null);
        final CalendarView calendar = (CalendarView) calendarLayout.getChildAt(0);
        calendar.setMinDate(new Date().getTime());
        try {
            calendar.setDate(dateFormat.parse(dateText.getText().toString()).getTime());
        } catch (ParseException e) {
            calendar.setDate(new Date().getTime());
        }
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view,
                                            int year, int month, int dayOfMonth) {
                dateText.setText(dayOfMonth + "-" + ++month + "-" + year);
            }
        });
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            calendar.setDate(dateFormat.parse(dateText.getText().toString()).getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dateText.setText(dateFormat.format(calendar.getDate()));
                    }
                });
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.setTitle(R.string.calendarTitle);
        alertDialog.setView(calendarLayout);
        alertDialog.show();
    }
    public void addAction(View view) {
        int amount = 0;

        EditText actionName = (EditText) findViewById(R.id.actionName);
        if(typeNumber == 0) {
            EditText hours = (EditText) typeLayout.getChildAt(0);
            EditText minutes = (EditText) typeLayout.getChildAt(2);
            amount = 0;
        }

        String name = actionName.getText().toString();
        String type = typeSpinner.getChildAt(typeNumber).toString();


    }
}
